package com.pgprint.app.utils

import com.pgprint.app.BuildConfig
import com.pgprint.app.model.AppVersion
import com.pgprint.app.model.AppVersionState
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
    private var _checkState = MutableStateFlow<AppVersionState>(AppVersionState.Checking)
    val checkState: StateFlow<AppVersionState> = _checkState.asStateFlow()

    suspend fun getCurrentAppVersion() {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                AppRequest.client.get("getLastAppVersionData").body<RequestResult<AppVersion>>()
            }
        }
        result.onSuccess {
            if (it.code == 200) {
                it.data?.let { data ->
                    if (Utils.compareVersion( data.version, BuildConfig.APP_VERSION) > 0) {
                        _checkState.value = AppVersionState.Update(data)
                    } else {
                        _checkState.value = AppVersionState.Usual
                    }
                    _state.value = data
                    HistoryLog.updateData("检查APP版本成功: ${data.version}")
                }
            } else {
                _checkState.value = AppVersionState.Error("检查APP版本失败: ${it.msg}")
                HistoryLog.updateData("检查APP版本失败: ${it.msg}")
            }
        }
        result.onFailure {
            _checkState.value = AppVersionState.Error("检查APP版本失败-${it.message}")
        }
    }
}
