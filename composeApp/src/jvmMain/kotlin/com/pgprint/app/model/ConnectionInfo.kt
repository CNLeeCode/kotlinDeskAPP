package com.pgprint.app.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ConnectionInfo(
    val key: String,
    val message: String,
    val time: Long,
) {
    fun showMsg(): String {
       return  "[${Instant.ofEpochMilli(time)
           .atZone(ZoneId.systemDefault())
           .format(DateTimeFormatter.ofPattern("HH:mm"))}]$message"

    }
}
