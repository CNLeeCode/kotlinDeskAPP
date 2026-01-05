package com.pgprint.app.model

import kotlinx.serialization.Serializable

@Serializable
data class ShopPrintOrder(
    val code: Int,
    val msg: String,
    val date: String,
    val data: List<ShopPrintOrderItem>
)
