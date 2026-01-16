package com.pgprint.app.usb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fazecast.jSerialComm.SerialPort
import com.github.anastaciocintra.escpos.EscPos
import com.github.anastaciocintra.escpos.EscPosConst
import com.github.anastaciocintra.escpos.Style
import com.github.anastaciocintra.escpos.barcode.BarCode
import com.github.anastaciocintra.escpos.barcode.QRCode
import com.github.anastaciocintra.escpos.image.BitImageWrapper
import com.github.anastaciocintra.escpos.image.Bitonal
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl
import com.github.anastaciocintra.escpos.image.EscPosImage
import com.pgprint.app.model.PrinterDevice
import com.pgprint.app.print.PrintManager
import com.pgprint.app.utils.EscPosPrinter
import com.pgprint.app.utils.PersistentCache
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.print.DocFlavor
import javax.print.PrintServiceLookup
import javax.usb.UsbDevice
import javax.usb.UsbHostManager
import javax.usb.UsbHub
import javax.usb.UsbInterface
// 定义设备数据类


fun getTestPrintData(): ByteArray {
    val baos = ByteArrayOutputStream()

    // 1. 初始化打印机 (ESC @)
    baos.write(byteArrayOf(0x1B, 0x40))

    // 2. 设置居中 (ESC a 1)
    baos.write(byteArrayOf(0x1B, 0x61, 0x01))

    // 3. 打印标题并换行 (使用 GBK 编码兼容中文)
    baos.write("热敏打印机测试\n".toByteArray(charset("GBK")))

    // 4. 设置左对齐 (ESC a 0)
    baos.write(byteArrayOf(0x1B, 0x61, 0x00))
    baos.write("项目: 打印机连接成功\n".toByteArray(charset("GBK")))
    baos.write("时间: 2025-12-27\n".toByteArray(charset("GBK")))

    // 5. 切纸或走纸 (GS V 66 0)
    baos.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00))

    return baos.toByteArray()
}

fun text2byteArray(text: String): ByteArray  {
    return text.toByteArray(charset("GBK"))
}

fun divider(): ByteArray {
    return text2byteArray("--------------------------------\n")
}

fun lineLR(left: String, right: String, width: Int = 32): ByteArray {
    val leftBytes = left.toByteArray(charset("GBK"))
    val rightBytes = right.toByteArray(charset("GBK"))
    val spaceCount = width - leftBytes.size - rightBytes.size
    return text2byteArray(left + " ".repeat(spaceCount.coerceAtLeast(1)) + right + "\n")
}


 suspend fun printImage2(): ByteArray {
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
    printer.scaleTextSize(3,3)
    printer.writeText("#130\n")
    printer.doubleSize(false)
    /* ===== 平台 / 门店 ===== */
    printer.bold(true)
    printer.feed(1)
    printer.scaleTextSize(2,2)
    printer.center()
    printer.writeText("美团闪购\n", Style().setJustification(EscPosConst.Justification.Center))
    printer.bold(false)
    printer.feed(1)
    printer.scaleTextSize(1,1)
    printer.center()
    printer.writeText("比优特超市（市府大路店）\n", Style().setJustification(EscPosConst.Justification.Center))
    printer.writeText("用户联", Style().setJustification(EscPosConst.Justification.Center))
    /* ===== 条形码 ===== */
    printer.barcode("2401939050332732270")
    printer.center()
    printer.writeText("2401939050332732270", Style().setJustification(EscPosConst.Justification.Center))
    printer.feed(1)
    printer.left()
    printer.divider()
    /* ===== 订单信息 ===== */
    printer.writeText("订单号：2401939050332732270\n")
    printer.writeText("立即送达  时间：1/6 14:20\n")
    printer.writeText("下单时间：1月6日13:30\n")
    printer.writeText("收件人：刘女士\n")
    printer.writeText("电话：13019365834,8265   133****\n")
    printer.writeText("*4500\n")
    printer.writeText("地址：为保护隐私地址已隐藏，可前\n")
    printer.writeText("往APP查看详情\n")

    printer.divider()

    /* ===== 备注 ===== */
    printer.writeText("备注：【如遇缺货】：缺货时电话与我沟通\n")

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
    printer.writeText("\n亲，如果您不满意，请联系我们，我们\n")
    printer.writeText("会服务到您满意为止！如果您满意，\n")
    printer.writeText("请小小鼓励我们一下，奖励我们五星好评哦！\n")
    printer.writeText("我们的客服电话：155 6601 2733\n")

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




fun printImage(): ByteArray {
    val out = ByteArrayOutputStream()
    val escpos = EscPos(out)

    // 1. 定义样式

    val barCode = BarCode()
    barCode.setJustification(EscPosConst.Justification.Center)
    barCode.setBarCodeSize(70, 250)
    escpos.write(barCode, "2401939050332732270", )
    escpos.feed(2)
    escpos.cut(EscPos.CutMode.FULL)
    escpos.close()

    return out.toByteArray()
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterScannerScreen() {
    // 存储扫描到的设备列表
    var devices by remember { mutableStateOf(listOf<PrinterDevice>()) }
    var isScanning by remember { mutableStateOf(false) }

    // 扫描函数
    fun scanDevices() {
        isScanning = true
        val newList = mutableListOf<PrinterDevice>()

        if (devices.isNotEmpty()) devices = emptyList()

        // 1. 扫描驱动打印机 (USB-A/Type-C 只要装了驱动都能搜到)
        val printServices = PrintServiceLookup.lookupPrintServices(DocFlavor.BYTE_ARRAY.AUTOSENSE, null)
        printServices.forEach {
            // newList.add(PrinterDevice(it.name, "系统驱动"))
        }

        // 2. 扫描串口设备 (Type-C 转串口或工业打印机直连)
        val serialPorts = SerialPort.getCommPorts()
        serialPorts.forEach {
            print(it.descriptivePortName)
           //  newList.add(PrinterDevice(it.descriptivePortName, "串口/Type-C", it.systemPortName))
        }

        devices = newList
        isScanning = false
    }

    // 初始进入时自动扫描一次
    LaunchedEffect(Unit) {
        scanDevices()
    }

    Scaffold {
        Column(modifier = Modifier.fillMaxSize().padding(it)) {
            Button(onClick = { scanDevices() }, enabled = !isScanning) {
                if (isScanning) {
                    Text("加载中")
                } else {
                    Text("刷新")
                }
            }
            if (devices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("未发现连接的 USB 打印机，请检查连接或驱动")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(devices) { device ->
                        PrinterCard(device)
                    }
                }
            }
        }
    }
}

@Composable
fun PrinterCard(device: PrinterDevice) {
    var statusMessage by remember { mutableStateOf("") }
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
//                Text(text = device.name, style = MaterialTheme.typography.bodySmall)
//                Text(text = "类型: ${device.type} ${if (device.port.isNotEmpty()) "[${device.port}]" else ""}",
//                    style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
            }
            Button(onClick = {
                // 测试指令：ESC @ (初始化) + "Hello Printer" + 换行
                statusMessage = ""
//                val success = if (device.type == "系统驱动") {
//                    PrintManager.printViaDriver(device.name, printImage())
//                } else {
//                    PrintManager.printViaSerial(device.port, printImage())
//                }
//                statusMessage = if (success) "打印指令已发送" else "连接失败"
            }) {
                Text("测试打印")
            }
        }
    }
    if (statusMessage.isNotEmpty()) {
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = if (statusMessage.contains("失败")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}