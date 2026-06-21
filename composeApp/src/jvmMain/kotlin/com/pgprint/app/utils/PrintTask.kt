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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

object PrintTask {
    val noticeMav = ClassLoader.getSystemResource("notice.wav")
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val dbDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val printQueue = Channel<ShopPrintOrderDetail>(capacity = 9999)
    val printQueueFlow = printQueue.receiveAsFlow()
    // 存储当前正在运行的任务 Job
    private val activeJobs = ConcurrentHashMap<String, Job>()
    // 存储已确认打印成功的订单号（内存 + DB 持久化），用于防止重复入队
    private val printedOrderIds = MutableStateFlow<Map<String, Map<String, ShopPrintOrderItem>>>(emptyMap())
    // 存储"正在打印中"的订单号（已入队但尚未确认打印成功），用于防止并发重复入队
    private val printingOrderIds = MutableStateFlow<Set<String>>(emptySet())
    // 观察平台 ID 列表
    private val _platformIds = MutableStateFlow<Set<String>>(emptySet())

    val printedOrderMapList = printedOrderIds.asStateFlow()

    val refundNotice = MutableSharedFlow<Long>(0)

    init {
        scope.launch {
            printQueueFlow.combine(PrintDevice.currentCheckedPrinterDevice) { data, device ->
                device to data
            }.filter { (device, _) -> device != null }
                .collect { (device, data) ->
                    val orderKey = "${data.platform}:${data.orderId}"
                    val result = withContext(Dispatchers.IO) {
                        PrinterManager.print(device!!, PrintTemplate.templateV1(data))
                    }
                    // 打印成功才确认标记，失败则清除"打印中"状态，允许下次重试
                    when (result) {
                        is PrintResult.Success -> confirmPrinted(data)
                        is PrintResult.Error -> {
                            println("打印失败 [${data.orderId}]: $result，将从打印中状态移除，等待重试")
                            printingOrderIds.update { it - orderKey }
                        }
                    }
                }
        }
    }

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
                val startTime = System.currentTimeMillis()
                println("开始 [$platformId] Time: $startTime")
                try {
                    val printDetails = executePrintCycle2(platformId, shopId)
                    if (printDetails.isNotEmpty()) {
                        // 🔑 入队前先持久化到 DB，保证重启后 loadPrintedOrdersFromDb 能恢复
                        val orderItems = printDetails.map {
                            ShopPrintOrderItem(orderId = it.orderId, daySeq = it.daySeq)
                        }
                        confirmPrintedDb(platformId, orderItems, shopId = shopId)

                        // 标记"打印中"，用于运行时内存去重（跨轮询）
                        val orderKeys = printDetails.map { "${it.platform}:${it.orderId}" }
                        printingOrderIds.update { it + orderKeys }
                        for (item in printDetails) {
                            printQueue.send(item)
                        }
                        println("平台 $platformId 入队 ${printDetails.size} 条订单（已预写DB）")
                    }
                } catch (e: Exception) {
                    println("平台 $platformId 执行出错: ${e.message}")
                    delay(5000)
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

    fun singlePrint(detail: ShopPrintOrderDetail) {
        scope.launch {
            printQueue.send(detail)
        }
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
            scope.launch(Dispatchers.IO) {
                DesktopAudioPlayer.play2(noticeMav)
            }
        }
        // 双重去重：已打印 + 正在打印中
        val filterOrders = filterUnprinted(platformId, orderIds.data.distinctBy { it.orderId })
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
        if (orderDetails.isEmpty()) return emptyList()
        createLogInfo("[${platformId}]获取打印信息成功！(${orderDetails.size}条)")
        // DB预写入在 startPollingTask 入队前完成，内存标记在 init 中打印成功后才更新
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
        DatabaseManager.database.cancelQueries.deleteOlderThanDate(currentDateFormat, shopId)
    }

    /**
     * 实际打印成功后调用：更新内存缓存 + 从"打印中"集合移除。
     * 注意：DB 写入已在 startPollingTask 入队前完成，此处不再重复。
     */
    private fun confirmPrinted(detail: ShopPrintOrderDetail) {
        val platformId = detail.platform
        val orderItem = ShopPrintOrderItem(orderId = detail.orderId, daySeq = detail.daySeq)
        val orderKey = "${platformId}:${detail.orderId}"

        // 1. 更新内存中的已打印缓存（用于 UI 显示 + filterUnprinted）
        printedOrderIds.update { oldMap ->
            val platformMap = oldMap[platformId].orEmpty().toMutableMap()
            platformMap[detail.orderId] = orderItem
            oldMap.toMutableMap().apply {
                put(platformId, platformMap)
            }
        }

        // 2. 从"打印中"集合移除
        printingOrderIds.update { it - orderKey }
    }

    fun clearPrintedOrderIds() {
        printedOrderIds.update { emptyMap() }
    }

    fun createLogInfo(message: String) = HistoryLog.updateData(message)

    private suspend fun confirmPrintedDb(
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

    /**
     * 标记已打印（用于手动打印/扫码打印等场景，直接确认已打印）
     */
    fun markPrinted(
        platformId: String,
        orders: List<ShopPrintOrderItem>,
        date: String = "",
        shopId: String = ""
    ) {
        printedOrderIds.update { oldMap ->
            val oldPlatformMap = oldMap[platformId].orEmpty()
            val newPlatformMap = oldPlatformMap.toMutableMap().apply {
                orders.forEach { order ->
                    put(order.orderId, order)
                }
            }
            oldMap.toMutableMap().apply {
                put(platformId, newPlatformMap)
            }
        }
        scope.launch {
            confirmPrintedDb(platformId, orders, date, shopId)
        }
    }

    fun filterUnprinted(
        platformId: String,
        orders: List<ShopPrintOrderItem>
    ): List<ShopPrintOrderItem> {
        val printedMap = printedOrderIds.value[platformId].orEmpty()
        val printingSet = printingOrderIds.value
        println("printedOrderIds ${platformId}-${printedMap.toList()}")
        // 双重过滤：已打印成功的 + 正在打印中的，都排除
        return orders.filterNot { it.orderId in printedMap || "${platformId}:${it.orderId}" in printingSet }
    }

}