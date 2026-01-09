package com.pgprint.app.utils

import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.ShopPrintOrder
import com.pgprint.app.model.ShopPrintOrderDetail
import com.pgprint.app.model.ShopPrintOrderItem
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

object PrintTask {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val dbDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val printQueue = Channel<ShopPrintOrderDetail>(capacity = 9999)
    val printQueueFlow =  printQueue.receiveAsFlow()
    // 存储当前正在运行的任务 Job
    private val activeJobs = ConcurrentHashMap<String, Job>()
    // 存储已打印的订单号，防止重复打印 (建议生产环境持久化)
    private val printedOrderIds = MutableStateFlow<Map<String, MutableMap<String, ShopPrintOrderItem>>>(emptyMap())
    // 观察平台 ID 列表
    private val _platformIds = MutableStateFlow<Set<String>>(emptySet())

    val printedOrderMapList = printedOrderIds.asStateFlow()

    val refundNotice = MutableSharedFlow<Long>(0)



    /**
     * 更新平台列表：自动对比差异，增加新任务，取消移除的任务
     */
    fun updatePlatforms(newList: Set<String>, shopId: String = "") {
        val currentList = _platformIds.value

        // 1. 找出需要新增的平台
        (newList - currentList).forEach { platformId ->
            startPollingTask(platformId, shopId)
        }

        // 2. 找出需要停止的平台
        (currentList - newList).forEach { platformId ->
            stopPollingTask(platformId)
        }

        _platformIds.value = newList
    }

    private fun startPollingTask(platformId: String, shopId: String = "") {
        if (activeJobs.containsKey(platformId)) {
            println("平台 $platformId 已经在运行")
            return
        }
        val job = scope.launch(Dispatchers.IO) {
            println("开始监听平台: $platformId")
            while (isActive) {
                try {
                    val printDetails = executePrintCycle2(platformId, shopId)
                    println("打印 printDetails: $platformId : $printDetails")
                    if (printDetails.isNotEmpty()) {
                        for (item in printDetails) {
                            printQueue.send(item)
                        }
//                        println("打印订单信息: $platformId : $printDetails")
                    }
                } catch (e: Exception) {
                    println("平台 $platformId 执行出错: ${e.message}")
                    delay(5000) // 出错后等待一段时间再重试
                }
                // 每轮任务执行完，等待一小段时间再进入下一轮（避免请求过频）
                delay(10000)
            }
        }
        activeJobs[platformId] = job
    }

    private fun stopPollingTask(platformId: String) {
        activeJobs[platformId]?.cancel()
        activeJobs.remove(platformId)
        println("停止监听平台: $platformId")
    }

    fun stopPollingTask() {
        // 1. 取消所有正在运行的协程
        activeJobs.forEach { (id, job) ->
            job.cancel()
            println("平台 $id 已停止")
        }
        // 2. 清空管理容器
        activeJobs.clear()
        // 3. 重置 UI 状态
        _platformIds.value = emptySet()
    }

    private suspend fun executePrintCycle2(platformId: String, shopId: String = ""): List<ShopPrintOrderDetail> {
       val orderIds = withContext(Dispatchers.IO) {
            runCatching {
                AppRequest.client.post("getDaySeq") {
                    setBody(
                        FormDataContent(
                            Parameters.build {
                                append("wmid", platformId)
                                append("shopid", shopId)
                                append("secret", "panyishigedashuaige")
                            }
                        )
                    )
                }.body<ShopPrintOrder>()
            }.getOrNull()
        }
        if (orderIds?.code != 200 || orderIds.data.isEmpty()) {
            return emptyList()
        }
        if (orderIds.refundNotice.isNotEmpty()) {
            scope.launch {
                refundNotice.tryEmit(System.currentTimeMillis())
            }
        }
        // 去重复订单
        val filterOrders = filterUnprinted(platformId, orderIds.data)
        if (filterOrders.isEmpty()) return emptyList()
        val orderDetails = withContext(Dispatchers.IO) {
            runCatching {
                AppRequest.client.post("getOrderList") {
                    setBody(
                        FormDataContent(
                            Parameters.build {
                                append("wmid", platformId)
                                append("shopid", shopId)
                                append("secret", "panyishigedashuaige")
                                filterOrders.forEach {
                                    append("orderid_list[]", it.orderId)
                                }
                            }
                        )
                    )
                }.body<RequestResult<List<ShopPrintOrderDetail>>>()
            }.getOrNull()?.data.orEmpty()
        }
        if (orderDetails.isEmpty()) return emptyList<ShopPrintOrderDetail>()
        createLogInfo("[${platformId}]获打印信息据成功！(${orderDetails.size}条)")
        markPrinted(platformId, orderDetails.map { ShopPrintOrderItem( orderId = it.orderId, daySeq = it.daySeq) }, orderIds.date, shopId)
        return orderDetails
    }


    suspend fun loadPrintedOrdersFromDb(shopId: String) = withContext(Dispatchers.IO) {
        val currentDate: LocalDate = LocalDate.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDateFormat: String = currentDate.format(formatter)
        val allPrinted = DatabaseManager.database.printorderQueries.selectByDate(currentDateFormat, shopId).executeAsList() // 查询数据库所有已打印订单
        val map: MutableMap<String, MutableMap<String, ShopPrintOrderItem>> = allPrinted
            .groupBy { it.platform_id }
            .mapValues { entry ->
                entry.value.associate { printed ->
                    printed.order_id to ShopPrintOrderItem(
                        daySeq = printed.day_seq,
                        orderId = printed.order_id
                    )
                }.toMutableMap()
            }.toMutableMap()

        printedOrderIds.value = map
    }

    suspend fun deleteYesterdayPrintOrders(shopId: String) = withContext(Dispatchers.IO) {
        val currentDate: LocalDate = LocalDate.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDateFormat: String = currentDate.format(formatter)
        DatabaseManager.database.printorderQueries.deleteOlderThanDate(currentDateFormat, shopId)
    }

    fun markPrinted(
        platformId: String,
        orders: List<ShopPrintOrderItem>,
        date: String = "",
        shopId: String = ""
    ) {
        printedOrderIds.update { map ->
            val newMap = map.toMutableMap()
            val orderMap = newMap.getOrPut(platformId) { mutableMapOf() }
            orders.forEach { order ->
                orderMap[order.orderId] =  order
            }
            newMap
        }
        scope.launch {
            markPrintedDb(
                platformId,
                orders,
                date,
                shopId
            )
        }
    }

    fun clearPrintedOrderIds() {
        printedOrderIds.update {
            emptyMap()
        }
    }


    fun createLogInfo(
        message: String
    ) = HistoryLog.updateData(message)

    suspend fun markPrintedDb(
        platformId: String,
        orders: List<ShopPrintOrderItem>,
        date: String = "",
        shopId: String = "",
    ) = withContext(dbDispatcher) {
            val currentDate: LocalDate = LocalDate.now()
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDateFormat: String = currentDate.format(formatter)
            DatabaseManager.database.transaction {
                orders.forEach {
                    DatabaseManager.database.printorderQueries.insertConnection(
                        platform_id = platformId,
                        order_id = it.orderId,
                        day_seq = it.daySeq,
                        date = date.ifEmpty { currentDateFormat },
                        shop_id = shopId
                    )
                }
            }
    }

    fun filterUnprinted (
        platformId: String,
        orders: List<ShopPrintOrderItem>
    ): List<ShopPrintOrderItem> {
        val printedMap = printedOrderIds.value[platformId].orEmpty()
        println("printedOrderIds ${platformId}-${printedMap.toList()}" )
        return orders.filterNot { it.orderId in printedMap }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun executePrintCycle(platformId: String, shopId: String = "") {
        // 1. 获取待打印订单
        flow {
            val res = AppRequest.client.post("getDaySeq") {
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("wmid", platformId)
                            append("shopid", shopId)
                            append("secret", "panyishigedashuaige")
                        }
                    )
                )
            }.body<ShopPrintOrder>()
            val items = if (res.code == 200 && res.data.isNotEmpty()) {
                res.data
            } else {
                emptyList<ShopPrintOrderItem>()
            }
            emit(items)
        }.map { items ->
            filterUnprinted(platformId, items)
        }.flowOn(Dispatchers.IO).flatMapLatest { orderIds ->
            if (orderIds.isNotEmpty()) {
                flow {
                    val orderDetail =  AppRequest.client.post("getOrderList") {
                        setBody(
                            FormDataContent(
                                Parameters.build {
                                    append("wmid", platformId)
                                    append("shopid", shopId)
                                    append("secret", "panyishigedashuaige")
                                    orderIds.forEach {
                                        append("orderid_list[]", it.orderId)
                                    }
                                }
                            )
                        )
                    }.body<RequestResult<List<ShopPrintOrderDetail>>>()
                    val items = if (orderDetail.code == 200 && orderDetail.data?.isNotEmpty() == true) {
                        orderDetail.data
                    } else {
                        emptyList<ShopPrintOrderDetail>()
                    }
                    emit(items)
                }.flowOn(Dispatchers.IO)
            } else {
               flow {
                   emit( emptyList<ShopPrintOrderDetail>())
               }
            }
        }.catch {
            emit( emptyList<ShopPrintOrderDetail>())
        }.filter {
            it.isNotEmpty()
        }.collect {
            println( "这里开始打印：${it}")
        }
    }

    // --- 以下为模拟网络请求的方法 ---
    private suspend fun fetchPendingOrders(id: String): List<String> {
        delay(500) // 模拟网络耗时
        return listOf("${id}_${System.currentTimeMillis() / 10000}")
    }

    private suspend fun fetchOrderItems(orderId: String): List<String> {
        delay(300)
        return listOf("商品A", "商品B", "商品C")
    }

    private fun printData(order: String, items: List<String>): Boolean {
        println("正在打印平台订单: ${order}, 商品: $items")
        return true
    }


}