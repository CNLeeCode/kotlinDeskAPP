package com.pgprint.app.router.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.pgprint.app.componentScope
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.model.PrinterDevice
import com.pgprint.app.utils.UsbDevices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.delayFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


interface HomeComponent {
    val printDeviceData: StateFlow<PrintDeviceData>
    fun getPrintDeviceData()
}

class DefaultHomeComponent (
    componentContext: ComponentContext,
): HomeComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    override val printDeviceData = MutableStateFlow<PrintDeviceData>(PrintDeviceData.None)

    override fun getPrintDeviceData() {
        if (printDeviceData.value is PrintDeviceData.Loading ) {
            return
        }
        if (printDeviceData.value !is PrintDeviceData.Loading ) {
            printDeviceData.value = PrintDeviceData.Loading
        }
        scope.launch {
            flow {
                val devices = UsbDevices.getDevicesList()
                emit(Result.success(devices))
            }
            .flowOn(Dispatchers.IO)
            .catch {
                emit(Result.failure(it))
            }
            .collectLatest { result ->
                delay(1000)
                result.onSuccess {
                    printDeviceData.value = PrintDeviceData.Success(data = it)
                }.onFailure {
                    printDeviceData.value = PrintDeviceData.Error(message = it.message ?: "获取设备失败")
                }
            }
        }
    }

    init {
        getPrintDeviceData()
    }

}