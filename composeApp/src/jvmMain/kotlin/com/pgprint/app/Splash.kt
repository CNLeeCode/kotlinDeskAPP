package com.pgprint.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.model.AppVersionState
import com.pgprint.app.router.component.SplashComponent
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.AppStrings
import com.pgprint.app.utils.UpdateManager
import com.pgprint.app.utils.Utils
import io.ktor.websocket.Frame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.refresh_24
import pgprint.composeapp.generated.resources.undraw_walking_outside
import pgprint.composeapp.generated.resources.undraw_winter_walk


@Composable
fun Splash(
    component: SplashComponent,
    modifier: Modifier = Modifier
) {
    val checkState by UpdateManager.checkState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        UpdateManager.getCurrentAppVersion()
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.undraw_walking_outside),
            contentDescription = "",
            modifier = Modifier.align(Alignment.BottomStart).offset(y = 70.dp, x = -(40).dp).size(500.dp),
        )

        UiStateContentView(
            modifier = Modifier.size(500.dp)
        ) {
            Crossfade(
                targetState = checkState
            ) { state ->
                when(state) {
                    is AppVersionState.Checking -> {
                        UiStateCheckingView()
                    }
                    is AppVersionState.Error ->  {
                        UiStateErrorView(
                            message = state.msg,
                            toLoginPage = component::toLoginPage,
                            onRefresh = remember {
                                {
                                    scope.launch {
                                        UpdateManager.getCurrentAppVersion()
                                    }
                                }
                            }
                        )
                    }
                    is AppVersionState.Update -> {
                        UiStateUpdateView(
                            version = state.data.version,
                            toLoginPage = component::toLoginPage,
                            toDownloadPage = Utils::openDownloadPage
                        )
                    }
                    is AppVersionState.Usual -> {
                        UiStateCheckingView(
                            message = "跳转中...",
                            invoke = component::toLoginPage
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UiStateErrorView(
    modifier: Modifier = Modifier,
    message: String = "",
    onRefresh: () -> Unit,
    toLoginPage: () -> Unit
) {
    Column(
        modifier = modifier.widthIn(400.dp, 500.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("检查更新失败", fontSize = 18.sp)
                Icon(
                    painter = painterResource(Res.drawable.refresh_24),
                    modifier = Modifier.size(20.dp).clickable(
                        onClick = onRefresh
                    ),
                    contentDescription = "refresh"
                )
            }
            TextButton(
                onClick = toLoginPage
            ) {
                Text("继续旧版本")
            }
        }
        Text("错误信息：${message}", fontSize = 16.sp, color = Color.Red)
    }
}


@Composable
fun UiStateUpdateView(
    modifier: Modifier = Modifier,
    version: String,
    toDownloadPage: () -> Unit,
    toLoginPage: () -> Unit
) {
    Row (
        modifier = modifier.width(400.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = Color.Red
                ) {
                    Text("V${version}", color = Color.White)
                }
            }
        ) {
            Button(
                onClick = toDownloadPage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryColor
                )
            ) {
                Text("下载新版本")
            }
        }

        OutlinedButton(
            onClick = toLoginPage
        ) {
            Text("继续旧版本")
        }
    }
}


@Composable
fun UiStateCheckingView(
    modifier: Modifier = Modifier,
    message: String = "检查更新中....",
    invoke: (() -> Unit)? = null,
) {

    LaunchedEffect(true) {
        invoke?.let {
            delay(1000)
            it()
        }
    }

    Row (
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = AppColors.PrimaryColor,
            strokeWidth = 4.dp,
            trackColor = Color.Gray,
            modifier = Modifier.size(32.dp)
        )
        Spacer(
            modifier = Modifier.width(10.dp)
        )
        Text(message, fontSize = 18.sp)
    }
}

@Composable
fun UiStateContentView(
    modifier: Modifier = Modifier,
    content:  @Composable (ColumnScope.() -> Unit)
) {
    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(50.dp)
    ) {
        Text(AppStrings.appName, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        content()
    }
}
