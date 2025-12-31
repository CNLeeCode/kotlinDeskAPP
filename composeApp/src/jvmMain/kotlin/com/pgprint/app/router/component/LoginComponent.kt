package com.pgprint.app.router.component

import com.arkivanov.decompose.ComponentContext

interface LoginComponent {
    fun toHomeAction(shopId: String)
}

class DefaultLoginComponent (
    componentContext: ComponentContext,
    val toHome: (shopId: String) -> Unit,
): LoginComponent, ComponentContext by componentContext {

    override fun toHomeAction(shopId: String) {
        toHome(shopId)
    }
}