package com.pgprint.app.utils

import com.fazecast.jSerialComm.SerialPort
import com.pgprint.app.model.PrinterTarget
import com.pgprint.app.model.SerialConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet


sealed class PrintResult {

    object Success : PrintResult()

    sealed class Error : PrintResult() {
        object DeviceNotFound : Error()
        object DeviceBusy : Error()
        data class IoError(val e: Throwable) : Error()
    }
}

object PrinterManager {
    private val mutex = Mutex()

    suspend fun print(
        target: PrinterTarget,
        data: ByteArray
    ): PrintResult = mutex.withLock {
        when (target) {
            is PrinterTarget.Driver ->
                printViaDriver(target.service, data)

            is PrinterTarget.Serial ->
                printViaSerial(target.portName, target.config, data)
        }
    }

    // ---------------- Driver ----------------

    private fun printViaDriver(
        service: PrintService,
        data: ByteArray
    ): PrintResult {
        return try {
            val flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE
            val job = service.createPrintJob()
            val doc = SimpleDoc(data, flavor, null)

            job.print(doc, HashPrintRequestAttributeSet())
            PrintResult.Success
        } catch (e: Exception) {
            PrintResult.Error.IoError(e)
        }
    }

    // ---------------- Serial ----------------

    private fun printViaSerial(
        portName: String,
        config: SerialConfig,
        data: ByteArray
    ): PrintResult {
        val port = SerialPort.getCommPort(portName).apply {
            baudRate = config.baudRate
            setNumDataBits(config.dataBits)
            setNumStopBits(config.stopBits)
            setParity(config.parity)
            setComPortTimeouts(
                SerialPort.TIMEOUT_WRITE_BLOCKING,
                0,
                0
            )
        }

        return try {
            if (!port.openPort()) {
                return PrintResult.Error.DeviceBusy
            }
            try {
                val written = port.writeBytes(data, data.size)
                if (written != data.size) {
                    PrintResult.Error.IoError(
                        IllegalStateException("Partial write $written/${data.size}")
                    )
                } else {
                    PrintResult.Success
                }
            } finally {
                port.closePort()
            }

        } catch (e: Exception) {
            PrintResult.Error.IoError(e)
        }
    }
}
