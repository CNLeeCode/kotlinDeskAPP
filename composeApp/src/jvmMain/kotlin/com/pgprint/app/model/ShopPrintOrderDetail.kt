package com.pgprint.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopPrintOrderDetail(

    @SerialName("day_seq")
    val daySeq: String,

    @SerialName("orderid")
    val orderId: String,

    @SerialName("shop_name")
    val shopName: String,

    @SerialName("shop_phone")
    val shopPhone: String,

    @SerialName("billing_time")
    val billingTime: String,

    @SerialName("address")
    val address: String = "",

    val remarks: String = "",

    @SerialName("jh_temperature")
    val temperature: String = ""
)
