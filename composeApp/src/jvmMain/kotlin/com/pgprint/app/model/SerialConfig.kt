package com.pgprint.app.model
import com.fazecast.jSerialComm.SerialPort
import kotlinx.serialization.Serializable

@Serializable
data class SerialConfig(
    val baudRate: Int = 9600,
    val dataBits: Int = 8,
    val stopBits: Int = SerialPort.ONE_STOP_BIT,
    val parity: Int = SerialPort.NO_PARITY
)
