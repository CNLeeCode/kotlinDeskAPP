package com.pgprint.app.model


data class PrinterDevice(
    val displayName: String,      // UI 显示
    val type: PrinterTypeEnum,
    // ---- 持久标识（存数据库）----
    val stableId: String,

    // ---- 运行时 ----
    val runtimePort: String? = null
)
