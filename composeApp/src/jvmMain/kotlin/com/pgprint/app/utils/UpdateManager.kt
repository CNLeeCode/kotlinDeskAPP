package com.pgprint.app.utils

import com.pgprint.app.model.UpdateState
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.system.exitProcess

object UpdateManager {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    suspend fun startUpdate(msiUrl: String) {
        try {
            _state.value = UpdateState.Checking

            val msiFile = File(
                System.getProperty("java.io.tmpdir"),
                "app_update.msi"
            )

            download(msiUrl, msiFile)

            _state.value = UpdateState.Installing
            install(msiFile)

            _state.value = UpdateState.Finished
            exitProcess(0)

        } catch (e: Exception) {
            _state.value = UpdateState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun download(url: String, target: File) {

        AppRequest.client.prepareGet(url).execute() { response ->
            val channel = response.bodyAsChannel()
            val total = response.contentLength() ?: -1

            var downloaded = 0L
            val buffer = ByteArray(8 * 1024)

            while (!channel.isClosedForRead) {
                val read = channel.readAvailable(buffer)
                if (read <= 0) break
                target.appendBytes(buffer.copyOf(read))
                downloaded += read

                _state.value = UpdateState.Downloading(
                    progress = if (total > 0) downloaded.toFloat() / total else 0f,
                    downloaded = downloaded,
                    total = total
                )
            }
        }
    }

    private fun install(msi: File) {
        WindowsInstaller.install(msi)
    }
}
