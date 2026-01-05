package com.pgprint.app.utils

import com.fazecast.jSerialComm.SerialPort
import com.pgprint.app.model.PrinterTarget
import com.pgprint.app.model.SerialConfig
import java.util.UUID
import javax.print.DocFlavor
import javax.print.PrintServiceLookup

object UsbDevices {
    /**
     * 获取当前设备系统驱动/USB等信息
     */
    fun getDevicesList (): List<PrinterTarget> {
        val devicesList = mutableListOf<PrinterTarget>()
        // 1. 扫描驱动打印机 (USB-A/Type-C 只要装了驱动都能搜到)
        val printServices = PrintServiceLookup.lookupPrintServices(DocFlavor.BYTE_ARRAY.AUTOSENSE, null)
        printServices.forEachIndexed { key, it ->
            devicesList.add(PrinterTarget.Driver("${key}-${ UUID.randomUUID()}", it.name, it))
        }
        // 2. 扫描串口设备 (Type-C 转串口或工业打印机直连)
        val serialPorts = SerialPort.getCommPorts()
        serialPorts.forEachIndexed { key, it ->
            devicesList.add(PrinterTarget.Serial("${key}-${ UUID.randomUUID()}", it.descriptivePortName, SerialConfig(), it.vendorID, it.productID, it.serialNumber))
        }
        return devicesList.toList()
    }
}