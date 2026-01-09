package com.pgprint.app.router.component

import com.arkivanov.decompose.ComponentContext
import com.pgprint.app.componentScope
import com.pgprint.app.utils.PrintTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface LoginComponent {
     fun toHomeAction(shopId: String)
     suspend fun initData(shopId: String)
}

class DefaultLoginComponent (
    componentContext: ComponentContext,
    val toHome: (shopId: String) -> Unit,
): LoginComponent, ComponentContext by componentContext {

    private val scope = componentContext.componentScope()

    override fun toHomeAction(shopId: String) {
        scope.launch {
            initData(shopId)
            withContext(Dispatchers.Main) {
                toHome(shopId)
            }
        }
    }

    override suspend fun initData(shopId: String)  = withContext(Dispatchers.IO) {
        PrintTask.deleteYesterdayPrintOrders(shopId)
        delay(60)
        PrintTask.loadPrintedOrdersFromDb(shopId)
    }

}