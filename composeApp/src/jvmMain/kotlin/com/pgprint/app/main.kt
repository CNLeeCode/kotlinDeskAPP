package com.pgprint.app

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.subscribe
import com.pgprint.app.component.AppFooter
import com.pgprint.app.router.DefaultRootComponent
import com.pgprint.app.router.RootContent
import com.pgprint.app.utils.AppRequest
import com.pgprint.app.utils.CrashHandler
import com.pgprint.app.utils.Utils
import io.ktor.client.request.head
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

fun LifecycleOwner.componentScope(): CoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lifecycle.subscribe(onDestroy = { scope.cancel() })
    return scope
}

fun main() = application {
    val version = BuildConfig.APP_VERSION
    System.setProperty("skiko.renderApi", "SOFTWARE")
    CrashHandler.init()

    val lifecycle = LifecycleRegistry()
    // Always create the root component outside Compose on the UI thread
    val root = Utils.runOnUiThread {
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
        )
    }
    val mainWindowState = rememberWindowState(
        size = DpSize(1200.dp, 900.dp),
    )

    LifecycleController(lifecycle, mainWindowState)

    Window(
        onCloseRequest = ::exitApplication,
        title = "PG外卖到家小票打印V$version",
        state = mainWindowState,
    ) {
        MenuBar {
            Menu("文件", mnemonic = 'F') {
                Item("新订单", onClick = { /* 执行逻辑 */ })
                Item("设置", onClick = { /* 打开设置 */ })
                Separator() // 分割线
                Item("退出", onClick = ::exitApplication)
            }

            Menu("帮助") {
                Item("检查更新", onClick = { })
                Item("错误日志", onClick = CrashHandler::onHandleOpenLogFolder)
                Item("关于PG打印", onClick = { })
            }
        }
        RootContent(component = root, modifier = Modifier.fillMaxSize().background(Color.Blue))
    }
}