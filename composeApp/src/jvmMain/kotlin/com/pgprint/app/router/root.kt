package com.pgprint.app.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.pgprint.app.App
import com.pgprint.app.Login
import com.pgprint.app.Splash
import com.pgprint.app.componentScope
import com.pgprint.app.router.component.DefaultHomeComponent
import com.pgprint.app.router.component.DefaultLoginComponent
import com.pgprint.app.router.component.DefaultSplashComponent
import com.pgprint.app.router.component.HomeComponent
import com.pgprint.app.router.component.LoginComponent
import com.pgprint.app.router.component.SplashComponent
import com.pgprint.app.utils.AppRequest
import com.pgprint.app.utils.DataStored
import io.ktor.client.plugins.timeout
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>
    val currentShopId: MutableStateFlow<String>
    // 定义可能的子页面
    sealed class Child {
        class Splash(val component: SplashComponent): Child()
        class Home(val component: HomeComponent, val shopId: String) : Child()
        class Login(val component: LoginComponent) : Child()
    }
}

// 实现类
class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    // 当前门店号

    override val currentShopId = MutableStateFlow<String>("")

    // 1. 定义导航器 (StackNavigation)
    private val navigation = StackNavigation<Config>()

    // 2. 初始化堆栈
    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Splash, // 初始页面
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(config: Config, context: ComponentContext): RootComponent.Child {
        return when (config) {
            is Config.Login ->  RootComponent.Child.Login(
                DefaultLoginComponent(context, toHome = {
                    navigation.replaceCurrent(Config.Home(it))
                }),
            )
            is Config.Home -> RootComponent.Child.Home(
                DefaultHomeComponent(
                    context,
                    toLogin = {
                        navigation.replaceCurrent(Config.Login)
                    }
                ),
                shopId = config.shopId
            )
            is Config.Splash -> RootComponent.Child.Splash(DefaultSplashComponent(
                context,
                    toLogin = {
                        navigation.replaceCurrent(Config.Login)
                    }
            ))
        }
    }

    // 定义配置（路由参数）
    @Serializable
    sealed class Config {

        @Serializable
        data class Home(val shopId: String) : Config()

        @Serializable
        object Login : Config()

        @Serializable
        object Splash : Config()
    }
}

// val LocalCurrentShopId = staticCompositionLocalOf { "" }
@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.childStack,
        modifier = modifier,
        animation = stackAnimation(fade()),
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.Splash -> Splash(component = child.component)
            is RootComponent.Child.Login -> Login(component = child.component)
            is RootComponent.Child.Home  -> App(shopId = child.shopId , component = child.component)
        }
    }
}