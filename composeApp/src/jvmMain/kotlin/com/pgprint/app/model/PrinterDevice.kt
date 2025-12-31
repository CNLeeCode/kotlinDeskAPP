package com.pgprint.app.model


data class PrinterDevice(
    val name: String,
    val type: String, // "驱动" 或 "串口"
    val port: String = ""
)
