package com.pgprint.app.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

object AppToast {
    val toastMsg = MutableSharedFlow<String>(1)

    suspend fun showToast(message: String) {
        toastMsg.emit(message)
    }
}