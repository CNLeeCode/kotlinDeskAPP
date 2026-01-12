package com.pgprint.app.model

import kotlinx.serialization.Serializable

@Serializable
data class AppVersion(
    val name: String = "",
    val version: String = "",
    val key: String = ""
)
