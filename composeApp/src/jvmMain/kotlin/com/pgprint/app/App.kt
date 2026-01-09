package com.pgprint.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import com.pgprint.app.component.DrawerContent
import com.pgprint.app.component.HistoryLogView
import com.pgprint.app.component.HomeHeader
import com.pgprint.app.component.PrintPlatformGrid
import com.pgprint.app.component.RefreshButton
import com.pgprint.app.component.SettingView
import com.pgprint.app.component.ToolItem
import com.pgprint.app.component.UsbView
import com.pgprint.app.model.ConnectionInfo
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.model.PrintPlatform
import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.ShopPrintOrderItem
import com.pgprint.app.model.UiState
import com.pgprint.app.router.LocalCurrentShopId
import com.pgprint.app.router.LocalNetworkStatus
import com.pgprint.app.router.component.HomeComponent
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.AppStrings
import com.pgprint.app.utils.DataStored
import com.pgprint.app.utils.DesktopAudioPlayer
import com.pgprint.app.utils.HistoryLog
import com.pgprint.app.utils.PrintDevice
import com.pgprint.app.utils.PrintTask
import com.pgprint.app.utils.PrintTemplate
import com.pgprint.app.utils.PrinterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.change_shop
import pgprint.composeapp.generated.resources.choosefile
import pgprint.composeapp.generated.resources.log
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
@Preview
fun App(component: HomeComponent, modifier: Modifier = Modifier) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val uiScope = rememberCoroutineScope()
    val loading by component.loading.collectAsState()
    val printDeviceData by PrintDevice.printDeviceData.collectAsState()
    val shopId by component.currentShopId.collectAsState()
    val localCurrentShopId =  LocalCurrentShopId.current
    val localNetworkStatus = LocalNetworkStatus.current
    val printPlatform by component.printPlatform.collectAsState()

    val printPlatformIds by component.printPlatformIds.collectAsState()

    val checkedPrintPlatform by component.checkedPrintPlatform.collectAsState()
    val historyLog by HistoryLog.historyLog.collectAsState()
    val currentCheckedPrinter by PrintDevice.currentCheckedPrinterName.collectAsState()
    val checkedPrintPlatformAll by component.checkedPrintPlatformAll.collectAsState(false)

    val currentCheckedPrinterDevice by PrintDevice.currentCheckedPrinterDevice.collectAsState()
    val printQueueFlow = PrintTask.printQueueFlow
    val refundNotice = PrintTask.refundNotice

    val printedOrderMapList by PrintTask.printedOrderMapList.collectAsState()

    LaunchedEffect(checkedPrintPlatform, currentCheckedPrinterDevice) {
        if (
            currentCheckedPrinterDevice != null && shopId.isNotEmpty()
        ) {
            PrintTask.updatePlatforms(checkedPrintPlatform.toSet(), shopId)
        }
        if (checkedPrintPlatform.isEmpty()) {
            PrintTask.stopPollingTask()
        }
    }


    LaunchedEffect(checkedPrintPlatform) {
        when (printPlatform) {
            is UiState.Success<RequestResult<List<PrintPlatform>>> -> {
                val isAll = checkedPrintPlatform.size == (printPlatform as UiState.Success<RequestResult<List<PrintPlatform>>>).data.data?.size
                component.onChangePrintPlatformAll(
                    isAll, if (!isAll) checkedPrintPlatform else printPlatformIds
                )
            }
            else -> {}
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

    LaunchedEffect(true) {
        refundNotice.filter { it > 0L }.collect {
            withContext(Dispatchers.IO) {
                DesktopAudioPlayer.play("notice.wav")
            }
        }
    }

    LaunchedEffect(localCurrentShopId) {
        component.refreshPrintPlatform()
        PrintDevice.getPrintDeviceData()
    }

    AppTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent()
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (loading) {
                    LinearProgressIndicator(
                        color = AppColors.PrimaryColor,
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White
                    )
                }
                AppHeaderView(
                    currentShop = "当前门店：${shopId}",
                    onChangeShopAction = component::onChangeShopAction
                )
                LoggedView(
                    modifier = modifier.fillMaxWidth().weight(1f),
                    printDeviceData = printDeviceData,
                    localNetworkStatusMessage = localNetworkStatus.message,
                    printPlatformState = printPlatform,
                    historyLog = historyLog,
                    checkedPrintPlatform = checkedPrintPlatform,
                    currentCheckedPrinter = currentCheckedPrinter,
                    printedOrderMapList = printedOrderMapList,
                    getPrintDeviceData = PrintDevice::getPrintDeviceData,
                    checkedPrintPlatformAll = checkedPrintPlatformAll,
                    refreshPrintPlatform = component::refreshPrintPlatform,
                    onChangeCheckedPrintPlatform = component::onChangeCheckedPrintPlatform,
                    onChangePrinter = component::saveCurrentCheckedPrinter,
                    onClickPrintTest = remember (currentCheckedPrinterDevice) {
                        {
                            component.onClickPrintTest(currentCheckedPrinterDevice)
                        }
                    },
                    onChangePrintPlatformAll = remember(printPlatformIds) {
                        {
                            component.onChangePrintPlatformAll(it, if (it) printPlatformIds else emptyList())
                        }
                    },
                    onClickOpenDrawer = {
                        uiScope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LoggedView(
    modifier: Modifier = Modifier,
    printDeviceData: PrintDeviceData,
    localNetworkStatusMessage: String = "",
    printPlatformState: UiState<RequestResult<List<PrintPlatform>>>,
    checkedPrintPlatform: List<String>,
    historyLog: List<ConnectionInfo>,
    currentCheckedPrinter: String,
    checkedPrintPlatformAll: Boolean,
    printedOrderMapList: Map<String, MutableMap<String, ShopPrintOrderItem>>,
    getPrintDeviceData: () -> Unit,
    refreshPrintPlatform: () -> Unit,
    onChangeCheckedPrintPlatform: (wmId: String) -> Unit,
    onChangePrinter: (String) -> Unit,
    onChangePrintPlatformAll: (Boolean) -> Unit,
    onClickPrintTest: () -> Unit,
    onClickOpenDrawer: () -> Unit,
) {
    Column(
        modifier = modifier.background(AppColors.WindowBackground).safeContentPadding().fillMaxSize(),
    ) {
        HomeHeader(
            modifier = Modifier.fillMaxWidth().height(300.dp)
        ) {
            val toolItemModifier = Modifier.widthIn(200.dp, 300.dp)
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
                        else -> {
                            Checkbox(
                                checked = checkedPrintPlatformAll,
                                onCheckedChange = onChangePrintPlatformAll,
                            )
                        }
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
                    onClickPrintTest = onClickPrintTest,
                    onClickOpenDrawer = onClickOpenDrawer,
                )
            }
        }

        Crossfade(
            targetState = printPlatformState,
            modifier =  Modifier.fillMaxWidth().weight(1f),
            animationSpec = tween(
                durationMillis = 1000,
                delayMillis = 500,
                easing = LinearEasing
            )
        ) { state ->
            when (state) {
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material.Text(
                            text = "异常：$state.message",
                            color = AppColors.ErrorRed,
                            fontSize = 16.sp
                        )
                    }
                }

                is UiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { }
                }

                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp,
                            color = AppColors.PrimaryColor,
                            modifier = Modifier.size(24.dp).offset(y = (-10).dp)
                        )
                    }
                }

                is UiState.Success<RequestResult<List<PrintPlatform>>> -> {
                    val printPlatform =  state.data
                    printPlatform.data?.let {
                        PrintPlatformGrid(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            printPlatformList = it,
                            printedOrderMapList = printedOrderMapList
                        )
                    }

                }
            }
        }
        AppFooter(
            text = localNetworkStatusMessage
        )
    }
}

@Composable
fun AppHeaderView(
    modifier: Modifier = Modifier,
    currentShop: String,
    onChangeShopAction: () -> Unit
) {
    AppHeader(
        modifier = modifier.height(40.dp),
        leadingContent = {
            Row (
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(currentShop, fontSize = 14.sp)
                Icon(
                    painter = painterResource(Res.drawable.change_shop),
                    contentDescription = "change",
                    tint = AppColors.PrimaryColor,
                    modifier = Modifier.size(24.dp).clickable(
                        onClick = onChangeShopAction
                    )
                )
            }
        },
        trailingContent = {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("当前版本：${APP_VERSION}", fontSize = 14.sp)
            }
        },
    )
}

