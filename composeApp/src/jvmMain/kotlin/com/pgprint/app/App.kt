package com.pgprint.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.AppTheme
import com.pgprint.app.component.AppFooter
import com.pgprint.app.component.ChoosePrintDeviceList
import com.pgprint.app.component.HomeHeader
import com.pgprint.app.component.PrinterView
import com.pgprint.app.component.RefreshButton
import com.pgprint.app.component.ToolItem
import com.pgprint.app.component.UsbView
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.router.LocalCurrentShopId
import com.pgprint.app.router.LocalNetworkStatus
import com.pgprint.app.router.component.HomeComponent
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.AppStrings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.refresh
import pgprint.composeapp.generated.resources.undraw_winter_walk

@Composable
@Preview
fun App(component: HomeComponent, modifier: Modifier = Modifier) {

    val printDeviceData by component.printDeviceData.collectAsState()
    val shopId by component.currentShopId.collectAsState()
    val localCurrentShopId =  LocalCurrentShopId.current
    val localNetworkStatus = LocalNetworkStatus.current


    LaunchedEffect(localCurrentShopId) {
        println("localCurrentShopId: $localCurrentShopId")
    }


    AppTheme {
        LoggedView(
            modifier = modifier.fillMaxSize(),
            printDeviceData = printDeviceData,
            localNetworkStatusMessage = localNetworkStatus.message,
            currentShop = "当前门店：${shopId}",
            getPrintDeviceData = component::getPrintDeviceData,
        )
    }
}


@Composable
fun LoggedView(
    modifier: Modifier = Modifier,
    printDeviceData: PrintDeviceData,
    localNetworkStatusMessage: String = "",
    currentShop: String = "",
    getPrintDeviceData: () -> Unit,
) {
    Column(
        modifier = modifier.background(AppColors.WindowBackground).safeContentPadding().fillMaxSize(),
    ) {
        Column (modifier = Modifier.fillMaxWidth().weight(1f)) {
            HomeHeader(
                modifier = Modifier.fillMaxWidth().height(260.dp)
            ) {
                val toolItemModifier = Modifier.weight(1f)
                ToolItem(
                    modifier = toolItemModifier,
                    title = AppStrings.choosePlatformTitle,
                    titleSuffix = {
                        RefreshButton {

                        }
                    }
                ) {
                    ChoosePrintDeviceList(
                        modifier = toolItemModifier,
                    )
                }
                ToolItem(
                    modifier = toolItemModifier,
                    title = AppStrings.choosePrintDeciveTitle,
                    titleSuffix = {
                        RefreshButton(
                            onClick = getPrintDeviceData
                        )
                    }
                ) {
                    UsbView(
                        modifier = toolItemModifier,
                        state = printDeviceData
                    )
                }
                ToolItem(
                    modifier = toolItemModifier,
                    title = "连接信息"
                ) {

                }
                ToolItem(
                    modifier = toolItemModifier,
                    title = currentShop
                ) {

                }
            }
        }
        AppFooter(
            text = localNetworkStatusMessage
        )
    }
}

