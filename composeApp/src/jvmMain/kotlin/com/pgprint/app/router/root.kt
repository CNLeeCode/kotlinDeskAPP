package com.pgprint.app.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.pgprint.app.App
import com.pgprint.app.componentScope
import com.pgprint.app.router.component.DefaultHomeComponent
import com.pgprint.app.router.component.HomeComponent
import com.pgprint.app.utils.AppRequest
import io.ktor.client.plugins.timeout
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

data class OnlineStatusData(
    val status: Int = 0,
    val message: String = "检查网络中..."
)


interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>
    val networkStatusData: MutableStateFlow<OnlineStatusData>
    // 定义可能的子页面
    sealed class Child {
        class Home(val component: HomeComponent) : Child()
    }
}

// 实现类
class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    override val networkStatusData = MutableStateFlow<OnlineStatusData>(OnlineStatusData())
    // 1. 定义导航器 (StackNavigation)
    private val navigation = StackNavigation<Config>()

    // 2. 初始化堆栈
    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Home, // 初始页面
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(config: Config, context: ComponentContext): RootComponent.Child {
        return when (config) {
            is Config.Home -> RootComponent.Child.Home(DefaultHomeComponent(context))
        }
    }

    init {
        startNetworkMonitor()
    }


    private suspend fun checkServerHealth(url: String): OnlineStatusData {
        return try {
            val response: HttpResponse = AppRequest.client.head(url) {
                // 设置较短的超时，避免卡死
                timeout {
                    requestTimeoutMillis = 3000
                    connectTimeoutMillis = 2000
                }
            }
            val result = response.status.isSuccess() || response.status == HttpStatusCode.Unauthorized
            OnlineStatusData(
                status = if (result) 1 else 2,
                message = if (result) "✅ 网络环境良好" else "⛔ 网络环境异常",
            )
        } catch (e: Exception) {
            OnlineStatusData(
                status = 2,
                message = "⛔ 网络环境异常: ${e.message.toString()}",
            )
        }
    }

    private fun startNetworkMonitor() {
        scope.launch(Dispatchers.IO) {
           // while (isActive) {
                // 执行检查逻辑
                networkStatusData.value = checkServerHealth("http://wm.butsdgc.com/")
               //delay(10000)
            // }
        }
    }

    // 定义配置（路由参数）
    @Serializable
    sealed class Config {
        @Serializable
        object Home : Config()
    }
}

val LocalNetworkStatus = staticCompositionLocalOf { OnlineStatusData() }

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    val networkStatusData by component.networkStatusData.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalNetworkStatus provides networkStatusData) {
        Children(
            stack = component.childStack,
            modifier = modifier,
            animation = stackAnimation(fade()),
        ) {
            when (val child = it.instance) {
                is RootComponent.Child.Home -> App(component = child.component)
            }
        }
    }
}