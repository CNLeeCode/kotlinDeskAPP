package com.pgprint.app

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    System.setProperty("skiko.renderApi", "SOFTWARE")
    val mainWindowState = rememberWindowState(
        size = DpSize(900.dp, 700.dp)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "PG打印",
        state = mainWindowState
    ) {
        App()
    }
}