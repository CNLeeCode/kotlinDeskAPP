package com.pgprint.app.utils

import com.pgprint.app.model.AppVersion
import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.UpdateState
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

object UpdateManager {

    private val _state = MutableStateFlow<AppVersion>(AppVersion())
    val state: StateFlow<AppVersion> = _state.asStateFlow()

    suspend fun getCurrentAppVersion() {
        val result =  withContext(Dispatchers.IO) {
            runCatching {
                AppRequest.client.get("getLastAppVersionData").body<RequestResult<AppVersion>>()
            }.getOrNull()
        }
        result?.let {
            if (it.code == 200) {
                it.data?.let { data ->
                    HistoryLog.updateData("检查APP版本成功: ${data.version}")
                    _state.value = data
                }
            } else {
                HistoryLog.updateData("检查APP版本失败: ${it.msg}")
            }
        }
    }
}
