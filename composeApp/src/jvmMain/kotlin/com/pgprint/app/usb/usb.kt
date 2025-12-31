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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.print.DocFlavor
import javax.print.PrintServiceLookup
import javax.usb.UsbDevice
import javax.usb.UsbHostManager
import javax.usb.UsbHub
import javax.usb.UsbInterface
// 定义设备数据类


fun getTestPrintData(): ByteArray {
    val baos = java.io.ByteArrayOutputStream()

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


fun printImage(): ByteArray {
    val out = ByteArrayOutputStream()
    val escpos = EscPos(out)

    // 1. 定义样式
    val headerStyle = Style()
        .setFontSize(Style.FontSize._2, Style.FontSize._2) // 2倍大小
        .setJustification(EscPosConst.Justification.Center)
        .setBold(true)

    val subHeaderStyle = Style()
        .setJustification(EscPosConst.Justification.Center)
        .setBold(true)

    // 2. 打印头部内容
    val title = text2byteArray("#12 美团外卖\n")
    escpos.write( title, 0, title.size ).setStyle(headerStyle)
    val shopName = text2byteArray("#12 美团外卖\n")
    escpos.write(shopName, 0, shopName.size).setStyle(subHeaderStyle)
    escpos.feed(5)
    // 7. 插入二维码（内置指令方式）
    escpos.write(byteArrayOf(0x1B, 0x40), 0, 2)
    val qrCode = QRCode()
    qrCode.setSize(10) // 设置二维码大小 (1-16)
    qrCode.setJustification(EscPosConst.Justification.Center)
    escpos.write(qrCode, "https://waimai.meituan.com/order/12345")

    val subHeader = text2byteArray("\n扫码查询订单进度\n")
    escpos.write(subHeader, 0, subHeader.size,).setStyle(subHeaderStyle)

    // 8. 结尾走纸并切纸
    escpos.feed(5)
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
            newList.add(PrinterDevice(it.name, "系统驱动"))
        }

        // 2. 扫描串口设备 (Type-C 转串口或工业打印机直连)
        val serialPorts = SerialPort.getCommPorts()
        serialPorts.forEach {
            print(it.descriptivePortName)
            newList.add(PrinterDevice(it.descriptivePortName, "串口/Type-C", it.systemPortName))
        }

        devices = newList
        isScanning = false
    }

    // 初始进入时自动扫描一次
    LaunchedEffect(Unit) {
        scanDevices()
    }

    Scaffold(
    ) {
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
                Text(text = device.name, style = MaterialTheme.typography.bodySmall)
                Text(text = "类型: ${device.type} ${if (device.port.isNotEmpty()) "[${device.port}]" else ""}",
                    style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
            }
            Button(onClick = {
                // 测试指令：ESC @ (初始化) + "Hello Printer" + 换行
                statusMessage = "";
                val success = if (device.type == "系统驱动") {
                    PrintManager.printViaDriver(device.name, printImage())
                } else {
                    PrintManager.printViaSerial(device.port, printImage())
                }
                statusMessage = if (success) "打印指令已发送" else "连接失败"
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