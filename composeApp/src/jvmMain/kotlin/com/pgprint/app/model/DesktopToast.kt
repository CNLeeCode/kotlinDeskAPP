package com.pgprint.app.model

data class DesktopToast(
    val message: String,
    val durationMillis: Long = 2000L
)
