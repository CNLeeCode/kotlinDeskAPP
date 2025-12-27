package com.pgprint.app.print

import com.fazecast.jSerialComm.SerialPort
import javax.print.DocFlavor
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet

object PrintManager {

    /**
     * 针对“系统驱动”类型的打印
     */
    fun printViaDriver(printerName: String, data: ByteArray): Boolean {
        return try {
            val flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE
            val services = PrintServiceLookup.lookupPrintServices(flavor, null)
            val service = services.find { it.name.contains(printerName, ignoreCase = true) }
                ?: return false

            val job = service.createPrintJob()
            val doc = SimpleDoc(data, flavor, null)
            job.print(doc, HashPrintRequestAttributeSet())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 针对“串口/Type-C”类型的打印
     */
    fun printViaSerial(portName: String, data: ByteArray): Boolean {
        val comPort = SerialPort.getCommPort(portName)
        // 设置串口参数，通常热敏打印机为 9600 或 115200 波特率
        comPort.baudRate = 9600
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0)

        return try {
            if (comPort.openPort()) {
                val bytesWritten = comPort.writeBytes(data, data.size)
                comPort.closePort()
                bytesWritten > 0 // 返回是否写入成功
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}