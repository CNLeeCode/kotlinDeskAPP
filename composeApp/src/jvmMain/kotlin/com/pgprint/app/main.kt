package com.pgprint.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.pgprint.app.utils.AppStrings
import com.pgprint.app.utils.CrashHandler
import com.pgprint.app.utils.DatabaseManager
import com.pgprint.app.utils.DesktopTool
import com.pgprint.app.utils.PersistentCache
import com.pgprint.app.utils.PrintTask
import com.pgprint.app.utils.Utils
import io.ktor.client.request.head
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.cjwt
import pgprint.composeapp.generated.resources.copyright
import pgprint.composeapp.generated.resources.exit
import pgprint.composeapp.generated.resources.file
import pgprint.composeapp.generated.resources.log
import pgprint.composeapp.generated.resources.setting
import pgprint.composeapp.generated.resources.update

fun LifecycleOwner.componentScope(): CoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lifecycle.subscribe(onDestroy = { scope.cancel() })
    return scope
}

@OptIn(ExperimentalMaterialApi::class)
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
        size = DpSize(1300.dp, 900.dp),
    )

    LifecycleController(lifecycle, mainWindowState)

    Window(
        onCloseRequest = ::exitApplication,
        title = "${AppStrings.appName}${version}",
        state = mainWindowState,
    ) {
        var loading by remember {
            mutableStateOf(true)
        }
        MenuBar {
            Menu("菜单", mnemonic = 'F') {
                Item("设置", onClick = { /* 打开设置 */ }, icon = painterResource(Res.drawable.setting))
                Separator() // 分割线
                Item("退出", onClick = ::exitApplication , icon = painterResource(Res.drawable.exit))
            }

            Menu("帮助") {
                Item("常见问题", onClick = { DesktopTool.openBrowser("https://www.baidu.com") }, icon = painterResource(Res.drawable.cjwt))
                Item("检查更新", onClick = { }, icon = painterResource(Res.drawable.update))
                Item("错误日志", onClick = CrashHandler::onHandleOpenLogFolder, icon = painterResource(Res.drawable.log))
                Item("缓存地址", onClick = PersistentCache::openCatchDir, icon = painterResource(Res.drawable.file))
                Separator() // 分割线
                Item("关于PG打印", onClick = { }, icon = painterResource(Res.drawable.copyright))
            }
        }
        LaunchedEffect(true) {
             delay(60)
             loading = false
        }

        CompositionLocalProvider(
            LocalMinimumInteractiveComponentEnforcement provides false
        ) {
            AnimatedContent(
                targetState = loading,
                label = "size transform"
            ) {
                if (it) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
                    ) {
                        Text("正在初始化...", fontSize = 16.sp)
                    }
                }
                if (!it) {
                    RootContent(component = root, modifier = Modifier.fillMaxSize().background(Color.Blue))
                }
            }
        }
    }
}