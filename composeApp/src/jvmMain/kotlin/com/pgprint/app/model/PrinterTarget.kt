package com.pgprint.app.model

import javax.print.PrintService

sealed class PrinterTarget {
    abstract val key: String
    abstract val portName: String

    data class Driver(
        override val key: String,
        override  val portName: String,
        val service: PrintService,
    ) : PrinterTarget()

    data class Serial(
        override val key: String,
        override  val portName: String,
        val config: SerialConfig,
        val vendorId: Int? = null,
        val productId: Int? = null,
        val serialNumber: String? = null
    ) : PrinterTarget()
}
