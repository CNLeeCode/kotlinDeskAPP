package com.pgprint.app.router.component

import com.arkivanov.decompose.ComponentContext
import com.pgprint.app.componentScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

interface SplashComponent {
    fun toLoginPage()
}

class DefaultSplashComponent (
    componentContext: ComponentContext,
    val toLogin: () -> Unit
): SplashComponent, ComponentContext by componentContext {
    private val scope = componentContext.componentScope()

    override fun toLoginPage() {
        toLogin()
    }

}