package com.pgprint.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopPrintOrderItem(

    @SerialName("day_seq")
    val daySeq: String,

    @SerialName("orderid")
    val orderId: String,
)