package com.pgprint.app.utils

import com.pgprint.app.model.ConnectionInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

object HistoryLog {

    private val _historyLog: MutableStateFlow<List<ConnectionInfo>> = MutableStateFlow(emptyList())

    val historyLog = _historyLog.asStateFlow()

    fun updateData(message: String, color: String = "#07c160") {
        _historyLog.update {
            listOf(ConnectionInfo(
                key = UUID.randomUUID().toString(),
                message = message,
                time = System.currentTimeMillis()
            )) + it
        }

    }
}