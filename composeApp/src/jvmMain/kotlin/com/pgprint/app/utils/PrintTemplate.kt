package com.pgprint.app.utils

import com.github.anastaciocintra.escpos.EscPosConst
import com.github.anastaciocintra.escpos.Style
import com.pgprint.app.model.ShopPrintOrderDetail
import java.io.ByteArrayOutputStream

object PrintTemplate {

    fun templateV1(
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
        printer.writeText("到家外卖\n", Style().setJustification(EscPosConst.Justification.Center))
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
        printer.writeText("立即送达  时间：1/6 14:20\n")
        printer.writeText("下单时间：1月6日13:30\n")
        printer.writeText("收件人：刘女士\n")
        printer.writeText("电话：13019365834,8265   133****\n")
        printer.writeText("*4500\n")
        printer.writeText("地址：${shopPrintOrderDetail.address}\n")

        printer.divider()

        /* ===== 备注 ===== */
        printer.writeText("备注：${shopPrintOrderDetail.remarks}\n")

        printer.divider()

        /* ===== 商品表头 ===== */
        printer.writeText("商品  数量            单价  金额")
        printer.feed(1)
        printer.divider()
        printer.writeText("1、象牛特仑苏纯牛奶250ml*12【比优特精选 整箱 高端 早餐优选】")
        printer.feed(1)
        printer.writeText("9987767987")
        printer.feed(1)
        /* ===== 商品 ===== */
        printer.product(
            name = "  ",
            qty = "X1",
            total = "53.47"
        )
        printer.feed(1)
        printer.divider()

        /* ===== 金额汇总 ===== */
        printer.lineLR("商品合计", "X1  53.47")
        printer.lineLR("配送费", "4.50")
        printer.lineLR("包装费", "0.00")
        printer.lineLR("优惠金额", "17.57")

        printer.divider()

        printer.bold(true)
        printer.lineLR("实付金额", "41.60")
        printer.bold(false)

        /* ===== 底部提示 ===== */
        printer.writeText("\n${shopPrintOrderDetail.temperature}\n")

        printer.qrcode("http://www.baidu.com")

        /* ===== 结束 ===== */
        printer.feed(5)
        printer.cut()
        printer.close()

        return out.toByteArray()
    }

}