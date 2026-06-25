package com.pgprint.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
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
import com.pgprint.app.router.DefaultRootComponent
import com.pgprint.app.router.RootContent
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.AppStrings
import com.pgprint.app.utils.AppToast
import com.pgprint.app.utils.CrashHandler
import com.pgprint.app.utils.PersistentCache
import com.pgprint.app.utils.UpdateBuilder
import com.pgprint.app.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.cjwt
import pgprint.composeapp.generated.resources.copyright
import pgprint.composeapp.generated.resources.exit
import pgprint.composeapp.generated.resources.file
import pgprint.composeapp.generated.resources.log
import pgprint.composeapp.generated.resources.update
import kotlin.system.exitProcess

fun LifecycleOwner.componentScope(): CoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lifecycle.subscribe(onDestroy = { scope.cancel() })
    return scope
}


@OptIn(ExperimentalMaterialApi::class)
fun main(args: Array<String>) = application {

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
        size = DpSize(1270.dp, 900.dp),
    )

    LifecycleController(lifecycle, mainWindowState)

    Window(
        onCloseRequest = ::exitApplication,
        title = "${AppStrings.appName}${version}",
        state = mainWindowState,
    ) {
        val snackBarHostState = remember { SnackbarHostState() }
        val appToastFLow = AppToast.toastMsg
        var aboutWindowShow by remember {
            mutableStateOf(false)
        }
        val uriHandler = LocalUriHandler.current
        MenuBar {
            Menu("菜单", mnemonic = 'F') {
//                Item("设置", onClick = { /* 打开设置 */ }, icon = painterResource(Res.drawable.setting))
                Item("常见问题", onClick = { uriHandler.openUri( Utils.openFAQPage())  }, icon = painterResource(Res.drawable.cjwt))
                Item("前往下载", onClick = {  uriHandler.openUri(Utils.openDownloadPage()) }, icon = painterResource(Res.drawable.update))
                Separator() // 分割线
                Item("退出", onClick = ::exitApplication , icon = painterResource(Res.drawable.exit))
            }

            Menu("帮助") {
                Item("错误日志", onClick = CrashHandler::onHandleOpenLogFolder, icon = painterResource(Res.drawable.log))
                Item("缓存地址", onClick = PersistentCache::openCatchDir, icon = painterResource(Res.drawable.file))
                Separator() // 分割线
                Item("关于PG打印", onClick = { aboutWindowShow = true }, icon = painterResource(Res.drawable.copyright))
            }
        }

        LaunchedEffect(true) {
            appToastFLow.filter { i -> i.isNotEmpty() }.collect {
                snackBarHostState.showSnackbar(it)
            }
        }

        CompositionLocalProvider(
            LocalMinimumInteractiveComponentEnforcement provides false
        ) {
            Box {
                RootContent(component = root, modifier = Modifier.fillMaxSize().background(
                    AppColors.WindowBackground)
                )
                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier
                        .align(Alignment.TopCenter) // 这里控制位置：TopCenter, TopStart 等
                        .padding(top = 20.dp)       // 避免贴太紧
                )
            }
        }

        if (aboutWindowShow) {
            Window(
                onCloseRequest = {
                    aboutWindowShow = false
                },
                state = rememberWindowState(
                    width = 400.dp,
                    height = 180.dp
                ),
                title = "关于PG打印",
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(AppStrings.appName + "V" + version, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("©️" + AppStrings.copyright, fontSize = 14.sp)
                }
            }
        }
    }
}