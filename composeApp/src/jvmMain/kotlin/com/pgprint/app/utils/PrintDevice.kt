package com.pgprint.app.utils

import com.pgprint.app.componentScope
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.model.PrinterTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PrintDevice {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val currentCheckedPrinterName = DataStored.currentCheckedPrinter.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )
    // 打印设备类别
    val printDeviceData = MutableStateFlow<PrintDeviceData>(PrintDeviceData.None)

    val currentCheckedPrinterDevice: StateFlow<PrinterTarget?> = combine(currentCheckedPrinterName, printDeviceData) { printerName, deviceState ->
        if (printerName.isNotEmpty()) {
            when(deviceState) {
                is PrintDeviceData.Success -> {
                    deviceState.data.firstOrNull { printerName == it.portName }
                }
                else -> null
            }
        } else {
            null
        }
    }.distinctUntilChanged().stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun getPrintDeviceData() {
        if (printDeviceData.value is PrintDeviceData.Loading) return
        printDeviceData.value = PrintDeviceData.Loading
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    UsbDevices.getDevicesList()
                }
            }.onSuccess {
                printDeviceData.value = PrintDeviceData.Success(data = it)
            }.onFailure {
                printDeviceData.value = PrintDeviceData.Error(message = it.message ?: "获取设备失败")
            }
        }
    }
}