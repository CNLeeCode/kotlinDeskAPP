package com.pgprint.app.utils

import com.fazecast.jSerialComm.SerialPort
import com.pgprint.app.model.PrinterDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.print.DocFlavor
import javax.print.PrintServiceLookup

object UsbDevices {
    /**
     * 获取当前设备系统驱动/USB等信息
     */
    fun getDevicesList (): List<PrinterDevice> {
        val devicesList = mutableListOf<PrinterDevice>()
        // 1. 扫描驱动打印机 (USB-A/Type-C 只要装了驱动都能搜到)
        val printServices = PrintServiceLookup.lookupPrintServices(DocFlavor.BYTE_ARRAY.AUTOSENSE, null)
        printServices.forEach {
            devicesList.add(PrinterDevice(it.name, "系统驱动"))
        }
        // 2. 扫描串口设备 (Type-C 转串口或工业打印机直连)
        val serialPorts = SerialPort.getCommPorts()
        serialPorts.forEach {
            print(it.descriptivePortName)
            devicesList.add(PrinterDevice(it.descriptivePortName, "串口/Type-C", it.systemPortName))
        }
        return devicesList.toList()
    }
}