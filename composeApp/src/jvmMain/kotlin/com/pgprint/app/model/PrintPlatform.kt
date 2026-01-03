package com.pgprint.app.model

import kotlinx.serialization.Serializable

@Serializable
data class PrintPlatform(
    val id: String,
    val label: String,
    val img: String,
)
