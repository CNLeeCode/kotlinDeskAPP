package com.pgprint.app.utils

import io.ktor.client.plugins.timeout
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.pgprint.app.BuildConfig.DOMAIN_URL
import com.pgprint.app.model.OnlineStatusData
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

object NetworkCheck {
    val networkStatusData = MutableStateFlow(OnlineStatusData())
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var activeJobs: Job? = null


    private suspend fun checkServerHealth(url: String): OnlineStatusData {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = AppRequest.client.head(url) {
                    // 设置较短的超时，避免卡死
                    timeout {
                        requestTimeoutMillis = 3000
                        connectTimeoutMillis = 2000
                    }
                }
                val result = response.status.isSuccess() || response.status == HttpStatusCode.Unauthorized
                OnlineStatusData(
                    status = if (result) 1 else 2,
                    message = if (result) "✅ 网络环境良好" else "⛔ 网络环境异常",
                )
            } catch (e: Exception) {
                OnlineStatusData(
                    status = 2,
                    message = "⛔ 网络环境异常: ${e.message.toString()}",
                )
            }
        }
    }

    fun singleCheck() {
        scope.launch {
            networkStatusData.value = checkServerHealth(DOMAIN_URL)
        }
    }


    fun keepCheck(delayTime: Long = 30000) {
        activeJobs = scope.launch {
            while (isActive) {
                networkStatusData.value = checkServerHealth(DOMAIN_URL)
                delay(delayTime)
            }
        }
    }

    fun stopKeepCheck() {
        activeJobs?.cancel()
        activeJobs = null
    }

}