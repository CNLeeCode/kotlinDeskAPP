package com.pgprint.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopPrintOrderGoodsItem(
    @SerialName("goods_name")
    val goodsName: String = "",

    @SerialName("barcode")
    val barcode: String = "",

    val count: String = "",

    val price: String = "",

)


@Serializable
data class ShopPrintOrderDetail(

    @SerialName("wmid")
    val platform: String= "",

    @SerialName("day_seq")
    val daySeq: String= "",

    @SerialName("orderid")
    val orderId: String = "",

    @SerialName("shop_name")
    val shopName: String = "",

    @SerialName("shop_phone")
    val shopPhone: String = "",

    @SerialName("billing_time")
    val billingTime: String = "",

    @SerialName("address")
    val address: String = "",

    val remarks: String = "",

    @SerialName("jh_temperature")
    val temperature: String = "",

    @SerialName("wmname")
    val platformName: String = "",

    val uptime: String = "",

    @SerialName("reserve_status")
    val orderType: String = "",

    @SerialName("count")
    val totalNum: String = "",

    @SerialName("package_bag_money")
    val packageBagMoney: String = "",

    @SerialName("shipping_fee")
    val shippingFee: String = "",

    @SerialName("item_price")
    val originalPrice: String = "",

    @SerialName("total")
    val totalFee: String = "",

    @SerialName("detail")
    val goodsList: List<ShopPrintOrderGoodsItem> = emptyList()

) {
    fun reserveOrderType(): String {
        if (orderType == "1") {
            return "及时单"
        }
        return "预约单"
    }
}
