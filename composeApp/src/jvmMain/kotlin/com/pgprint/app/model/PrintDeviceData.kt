package com.pgprint.app.model

sealed interface PrintDeviceData {
    object None: PrintDeviceData
    object Loading: PrintDeviceData
    data class Error(val message: String): PrintDeviceData
    data class Success(val data: List<PrinterTarget>): PrintDeviceData
}