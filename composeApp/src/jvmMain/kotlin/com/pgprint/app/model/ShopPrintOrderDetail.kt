package com.pgprint.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopPrintOrderDetail(

    @SerialName("day_seq")
    val daySeq: String,

    @SerialName("shop_name")
    val shopName: String,

    @SerialName("shop_phone")
    val shopPhone: String,

    @SerialName("billing_time")
    val billingTime: String,

    @SerialName("address")
    val address: String,

)
