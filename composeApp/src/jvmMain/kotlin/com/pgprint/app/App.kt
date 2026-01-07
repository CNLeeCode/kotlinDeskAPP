package com.pgprint.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import com.example.compose.AppTheme
import com.pgprint.app.BuildConfig.APP_VERSION
import com.pgprint.app.component.AppFooter
import com.pgprint.app.component.AppHeader
import com.pgprint.app.component.CheckUpDateButton
import com.pgprint.app.component.ChoosePrintDeviceList
import com.pgprint.app.component.HistoryLogView
import com.pgprint.app.component.HomeHeader
import com.pgprint.app.component.RefreshButton
import com.pgprint.app.component.SettingView
import com.pgprint.app.component.ToolItem
import com.pgprint.app.component.UpdateDialog
import com.pgprint.app.component.UsbView
import com.pgprint.app.model.ConnectionInfo
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.model.PrintPlatform
import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.UiState
import com.pgprint.app.utils.PrinterManager
import com.pgprint.app.router.LocalCurrentShopId
import com.pgprint.app.router.LocalNetworkStatus
import com.pgprint.app.router.component.HomeComponent
import com.pgprint.app.usb.getTestPrintData
import com.pgprint.app.usb.printImage
import com.pgprint.app.usb.printImage2
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.AppStrings
import com.pgprint.app.utils.DataStored
import com.pgprint.app.utils.DatabaseManager
import com.pgprint.app.utils.PrintDevice
import com.pgprint.app.utils.PrintTask
import com.pgprint.app.utils.PrintTemplate
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(component: HomeComponent, modifier: Modifier = Modifier) {

    val uiScope = rememberCoroutineScope()
    val printDeviceData by PrintDevice.printDeviceData.collectAsState()
    val shopId by component.currentShopId.collectAsState()
    val localCurrentShopId =  LocalCurrentShopId.current
    val localNetworkStatus = LocalNetworkStatus.current
    val printPlatform by component.printPlatform.collectAsState()
    val checkedPrintPlatform by component.checkedPrintPlatform.collectAsState()
    val historyLog by component.HistoryLog.collectAsState()
    val currentCheckedPrinter by PrintDevice.currentCheckedPrinterName.collectAsState()
    val currentPrintPlatformIds by PrintTask.platformIds.collectAsState()
    val currentCheckedPrinterDevice by PrintDevice.currentCheckedPrinterDevice.collectAsState()
    val printQueueFlow = PrintTask.printQueueFlow
    val updateState by component.updateManagerState.collectAsState()
    var alert by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(checkedPrintPlatform, currentCheckedPrinterDevice) {
        if (
            currentCheckedPrinterDevice != null && shopId.isNotEmpty()
        ) {
            PrintTask.updatePlatforms(checkedPrintPlatform.toSet(), shopId)
        }
    }

    LaunchedEffect(currentCheckedPrinterDevice) {
        if (currentCheckedPrinterDevice == null) {
            PrintTask.stopPollingTask()
        }
        currentCheckedPrinterDevice?.let { device ->
            printQueueFlow.collect { data ->
                print("接收并且开始打印 $data")
                withContext(Dispatchers.IO) {
                    PrinterManager.print(device, PrintTemplate.templateV1(data))
                }
            }
        }
    }

    LaunchedEffect(localCurrentShopId) {
        component.refreshPrintPlatform()
        PrintDevice.getPrintDeviceData()
    }

    if (alert) {
        Window(
            onCloseRequest = {
                alert = false
            },
        ) {
            UpdateDialog(
                state = updateState,
            ) {
                component.updateManagerDownLoad("http://39.98.37.44/apk/pgprinter-1.0.2.msi");
            }
        }
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
            currentCheckedPrinter = currentCheckedPrinter,
            getPrintDeviceData = PrintDevice::getPrintDeviceData,
            refreshPrintPlatform = component::refreshPrintPlatform,
            onChangeCheckedPrintPlatform = component::onChangeCheckedPrintPlatform,
            onChangePrinter = component::saveCurrentCheckedPrinter,
            onClickPrintTest = {
                    currentCheckedPrinterDevice?.let {
                    uiScope.launch(Dispatchers.IO) {
                        try {
                            PrinterManager.print(it, printImage2())
                        } catch (err: Throwable) {
                            print(err.message)
                        }
                    }
                }
            },
            checkUpDateAction = {
                alert = true
            }
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
    currentCheckedPrinter: String,
    getPrintDeviceData: () -> Unit,
    refreshPrintPlatform: () -> Unit,
    onChangeCheckedPrintPlatform: (wmId: String) -> Unit,
    onChangePrinter: (String) -> Unit,
    onClickPrintTest: () -> Unit,
    checkUpDateAction:  () -> Unit,
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
                            CheckUpDateButton(
                                onClick = checkUpDateAction
                            )
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
                    title = AppStrings.choosePrintDeciveTitle,
                    titleSuffix = {
                        RefreshButton(
                            onClick = getPrintDeviceData
                        )
                    }
                ) {
                    UsbView(
                        modifier = toolItemModifier,
                        state = printDeviceData,
                        currentCheckedPrinter = currentCheckedPrinter,
                        onChangePrinter = onChangePrinter,
                    )
                }
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
                    title = "连接信息",
                ) {
                    HistoryLogView(
                        historyLog = historyLog
                    )
                }
                ToolItem(
                    modifier = toolItemModifier,
                ) {
                    SettingView(
                        onClickPrintTest = onClickPrintTest
                    )
                }
            }
        }
        AppFooter(
            text = localNetworkStatusMessage
        )
    }
}

