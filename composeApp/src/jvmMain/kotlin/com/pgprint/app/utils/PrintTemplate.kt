package com.pgprint.app.utils

import com.github.anastaciocintra.escpos.EscPosConst
import com.github.anastaciocintra.escpos.Style
import com.pgprint.app.model.ShopPrintOrderDetail
import java.io.ByteArrayOutputStream
import java.io.File

object PrintTemplate {

   suspend fun templateV1(
        shopPrintOrderDetail: ShopPrintOrderDetail
    ): ByteArray {
        val out = ByteArrayOutputStream()
        val printer = EscPosPrinter(
            output = out,
            paperWidth = EscPosPrinter.WIDTH_58
        )
        printer.init()
        /* ===== 顶部状态 ===== */
        /* ===== 顶部状态 ===== */
        printer.center()
        /* ===== 大号订单号 ===== */
        printer.doubleSize(true)
        if (Utils.hasMiddleSpace(shopPrintOrderDetail.daySeq)) {
            printer.scaleTextSize(2,2)
            printer.writeText("#${shopPrintOrderDetail.daySeq}\n")
        } else {
            printer.scaleTextSize(3,3)
            printer.writeText("#${shopPrintOrderDetail.daySeq}\n")
        }
        printer.doubleSize(false)
        /* ===== 平台 / 门店 ===== */
        printer.bold(true)
        printer.feed(1)
        printer.scaleTextSize(2,2)
        printer.center()
        printer.writeText("${shopPrintOrderDetail.platformName}\n", Style().setJustification(EscPosConst.Justification.Center))
        printer.bold(false)
        printer.feed(1)
        printer.scaleTextSize(1,1)
        printer.center()
        printer.writeText("${shopPrintOrderDetail.shopName}\n", Style().setJustification(EscPosConst.Justification.Center))
        printer.writeText("用户联", Style().setJustification(EscPosConst.Justification.Center))
        /* ===== 条形码 ===== */
        printer.barcode(shopPrintOrderDetail.orderId)
        printer.center()
        printer.writeText(shopPrintOrderDetail.orderId, Style().setJustification(EscPosConst.Justification.Center))
        printer.feed(1)
        printer.left()
        printer.divider()
        /* ===== 订单信息 ===== */
        printer.writeText("订单号：${shopPrintOrderDetail.orderId}\n")
        printer.writeText("订单类型：${shopPrintOrderDetail.reserveOrderType()}\n")
        printer.writeText("下单时间：${shopPrintOrderDetail.uptime}\n")
//        printer.writeText("收件人：刘女士\n")
//        printer.writeText("电话：13019365834,8265   133****\n")
//        printer.writeText("*4500\n")
        printer.writeText("地址：${shopPrintOrderDetail.address}\n")

        printer.divider()

        /* ===== 备注 ===== */
        printer.writeText("${shopPrintOrderDetail.remarks}\n")

        printer.divider()

        /* ===== 商品表头 ===== */
        printer.writeText("商品  数量            单价  金额")
        printer.feed(1)
        printer.divider()
        // 商品循环

       for ((index, value) in shopPrintOrderDetail.goodsList.withIndex()) {
           printer.writeText("${index + 1}、${value.goodsName}")
           printer.feed(1)
           printer.writeText(value.barcode)
           printer.feed(1)
           /* ===== 商品 ===== */
           printer.product(
               name = "  ",
               qty = "X${value.count}",
               total = "${value.price}"
           )
           printer.feed(1)
       }

       // 商品循环


        printer.divider()

        /* ===== 金额汇总 ===== */
        printer.lineLR("商品合计", "X${shopPrintOrderDetail.totalNum}  ${shopPrintOrderDetail.originalPrice}")
        printer.lineLR("配送费", shopPrintOrderDetail.shippingFee)
        printer.lineLR("包装费", shopPrintOrderDetail.packageBagMoney)
//        printer.lineLR("优惠金额", "17.57")

        printer.divider()

        printer.bold(true)
        printer.lineLR("实付金额", shopPrintOrderDetail.totalFee)
        printer.bold(false)

        /* ===== 底部提示 ===== */
        printer.writeText("\n${shopPrintOrderDetail.temperature}\n")

        val kfImageFile = File(PersistentCache.cacheDir.absolutePath,"kf-photo.jpg")
        if (kfImageFile.exists()) {
           printer.center()
           printer.localImage(kfImageFile)
        }

        /* ===== 结束 ===== */
        printer.feed(5)
        printer.cut()
        printer.close()

        return out.toByteArray()
    }

}