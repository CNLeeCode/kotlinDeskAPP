package com.pgprint.app.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestResult <T>(
    val code: Int,
    val msg: String,
    val data: T?
)
