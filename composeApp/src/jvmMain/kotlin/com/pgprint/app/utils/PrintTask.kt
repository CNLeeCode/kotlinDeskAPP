package com.pgprint.app.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val job = scope.launch(Dispatchers.IO) {
            println("开始监听平台: $platformId")
            while (isActive) {
                try {

                    executePrintCycle(platformId)
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

    private suspend fun executePrintCycle(platformId: String) {
        // 1. 获取待打印订单
        val pendingOrders = fetchPendingOrders(platformId)

        for (order in pendingOrders) {
            // 2. 防重检查
            if (printedOrderIds.contains(order)) continue

            // 3. 根据订单获取商品详情
            val items = fetchOrderItems(order)

            // 4. 打印数据
            val success = printData(order, items)

            if (success) {
                printedOrderIds.add(order)
                // 限制内存占用：如果集合过大，可以清理早期数据
                if (printedOrderIds.size > 1000) printedOrderIds.remove(printedOrderIds.first())
            }
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