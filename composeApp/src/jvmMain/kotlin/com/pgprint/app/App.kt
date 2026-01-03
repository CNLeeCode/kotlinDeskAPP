package com.pgprint.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AppTheme
import com.pgprint.app.BuildConfig.APP_VERSION
import com.pgprint.app.component.AppFooter
import com.pgprint.app.component.AppHeader
import com.pgprint.app.component.CheckUpDateButton
import com.pgprint.app.component.ChoosePrintDeviceList
import com.pgprint.app.component.HistoryLogView
import com.pgprint.app.component.HomeHeader
import com.pgprint.app.component.RefreshButton
import com.pgprint.app.component.ToolItem
import com.pgprint.app.component.UsbView
import com.pgprint.app.model.ConnectionInfo
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.model.PrintPlatform
import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.UiState
import com.pgprint.app.router.LocalCurrentShopId
import com.pgprint.app.router.LocalNetworkStatus
import com.pgprint.app.router.component.HomeComponent
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.AppStrings
import com.pgprint.app.utils.DatabaseManager
import com.pgprint.app.utils.PrintTask
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(component: HomeComponent, modifier: Modifier = Modifier) {

    val printDeviceData by component.printDeviceData.collectAsState()
    val shopId by component.currentShopId.collectAsState()
    val localCurrentShopId =  LocalCurrentShopId.current
    val localNetworkStatus = LocalNetworkStatus.current
    val printPlatform by component.printPlatform.collectAsState()
    val checkedPrintPlatform by component.checkedPrintPlatform.collectAsState()
    val historyLog by component.HistoryLog.collectAsState()

    val currentPrintPlatformIds by PrintTask.platformIds.collectAsState()

    LaunchedEffect(checkedPrintPlatform) {
        println("checkedPrintPlatform ${checkedPrintPlatform.toString()}")
        PrintTask.updatePlatforms(checkedPrintPlatform.toSet(), "2309")

    }


    LaunchedEffect(currentPrintPlatformIds) {
        println("currentPrintPlatformIds $currentPrintPlatformIds")
    }



    LaunchedEffect(localCurrentShopId) {
       //  println("localCurrentShopId: $localCurrentShopId")
       //  component.callPrintPlatform.tryEmit(Unit)
        component.refreshPrintPlatform()
    }

    LaunchedEffect(printPlatform) {
        println("printPlatform" + printPlatform.toString())
        when(printPlatform) {
            is UiState.Error -> {
                println("Error")
            }
            is UiState.Idle -> {
                println("Idle")
            }
            is UiState.Loading -> {
                println("Loading")
            }
            is UiState.Success<*> -> {
                println("Success")
            }
        }
    }

    LaunchedEffect(true) {
       // val db = DatabaseManager.database
       //  val connQueries = db.connectionInfoQueries
    }

    AppTheme {
        LoggedView(
            modifier = modifier.fillMaxSize(),
            printDeviceData = printDeviceData,
            localNetworkStatusMessage = localNetworkStatus.message,
            currentShop = "当前门店：${shopId}",
            printPlatformState = printPlatform,
            historyLog = historyLog,
            checkedPrintPlatform = checkedPrintPlatform,
            getPrintDeviceData = component::getPrintDeviceData,
            refreshPrintPlatform = component::refreshPrintPlatform,
            onChangeCheckedPrintPlatform = component::onChangeCheckedPrintPlatform
        )
    }
}

@Composable
fun LoggedView(
    modifier: Modifier = Modifier,
    printDeviceData: PrintDeviceData,
    localNetworkStatusMessage: String = "",
    currentShop: String = "",
    printPlatformState: UiState<RequestResult<List<PrintPlatform>>>,
    checkedPrintPlatform: List<String>,
    historyLog: List<ConnectionInfo>,
    getPrintDeviceData: () -> Unit,
    refreshPrintPlatform: () -> Unit,
    onChangeCheckedPrintPlatform: (wmId: String) -> Unit,
) {
    Column(
        modifier = modifier.background(AppColors.WindowBackground).safeContentPadding().fillMaxSize(),
    ) {
        Row (
            modifier = Modifier.height(50.dp)
        ) {
            AppHeader(
                leadingContent = {
                    Text(currentShop, fontSize = 14.sp)
                },
                trailingContent = {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("当前版本：${APP_VERSION}", fontSize = 14.sp)
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Text("1.0.3", fontSize = 8.sp)
                                }
                            }
                        ) {
                            CheckUpDateButton {

                            }
                        }
                    }
                },
            )
        }
        Column (modifier = Modifier.fillMaxWidth().weight(1f)) {
            HomeHeader(
                modifier = Modifier.fillMaxWidth().height(260.dp)
            ) {
                val toolItemModifier = Modifier.weight(1f)
                ToolItem(
                    modifier = toolItemModifier,
                    title = AppStrings.choosePlatformTitle,
                    titleSuffix = {
                        when(printPlatformState) {
                            is UiState.Error -> {
                                RefreshButton {
                                    refreshPrintPlatform()
                                }
                            }
                            else -> {}
                        }
                    }
                ) {
                    ChoosePrintDeviceList(
                        modifier = toolItemModifier,
                        printPlatformState = printPlatformState,
                        checkedPrintPlatform = checkedPrintPlatform,
                        onChangeCheckedPrintPlatform = onChangeCheckedPrintPlatform,
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
                    title = "连接信息",
                ) {
                    HistoryLogView(
                        historyLog = historyLog
                    )
                }
                ToolItem(
                    modifier = toolItemModifier,
                ) {
                    Text("ToolItem")
                }
            }
        }
        AppFooter(
            text = localNetworkStatusMessage
        )
    }
}

