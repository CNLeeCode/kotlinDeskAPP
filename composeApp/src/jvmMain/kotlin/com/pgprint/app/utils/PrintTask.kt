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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object PrintTask {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // 存储当前正在运行的任务 Job
    private val activeJobs = ConcurrentHashMap<String, Job>()
    // 存储已打印的订单号，防止重复打印 (建议生产环境持久化)
    private val printedOrderIds = mutableSetOf<String>()
    // 观察平台 ID 列表
    private val _platformIds = MutableStateFlow<Set<String>>(emptySet())
    val platformIds = _platformIds.asStateFlow()

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
            while (isActive) {62900
                try {
                    executePrintCycle(platformId, shopId)
                } catch (e: Exception) {
                    println("平台 $platformId 执行出错: ${e.message}")
                    delay(5000) // 出错后等待一段时间再重试
                }
                // 每轮任务执行完，等待一小段时间再进入下一轮（避免请求过频）
                delay(2000)
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
            val newItems = items.filter { it.orderId !in printedOrderIds }
            printedOrderIds.addAll(newItems.map { it.orderId })
            newItems
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