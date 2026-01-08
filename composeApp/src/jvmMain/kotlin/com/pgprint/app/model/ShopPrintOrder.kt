package com.pgprint.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable
data class RefundNoticeItem(
    @SerialName("wmid")
    val platform: String,
)


@Serializable
data class ShopPrintOrder(
    val code: Int,
    val msg: String,
    val date: String,
    val data: List<ShopPrintOrderItem> = emptyList(),
    @SerialName("refund_notice")
    val refundNotice: List<RefundNoticeItem> = emptyList()
)
