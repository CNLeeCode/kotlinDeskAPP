package com.pgprint.app.router.component

import androidx.compose.runtime.collectAsState
import com.arkivanov.decompose.ComponentContext
import com.pgprint.app.componentScope
import com.pgprint.app.model.PrintPlatform
import com.pgprint.app.model.PrinterTarget
import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.ShopPrintOrderDetail
import com.pgprint.app.model.UiState
import com.pgprint.app.model.UpdateState
import com.pgprint.app.usb.printImage2
import com.pgprint.app.utils.AppRequest
import com.pgprint.app.utils.AppToast
import com.pgprint.app.utils.DataStored
import com.pgprint.app.utils.HistoryLog
import com.pgprint.app.utils.NetworkCheck
import com.pgprint.app.utils.PrintTask
import com.pgprint.app.utils.PrinterManager
import com.pgprint.app.utils.UpdateManager
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import io.ktor.util.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


interface HomeComponent {
    val currentShopId: StateFlow<String>
    val callPrintPlatform: MutableSharedFlow<Unit>
    val printPlatform: StateFlow<UiState<RequestResult<List<PrintPlatform>>>>
    val printPlatformIds: StateFlow<List<String>>
    val checkedPrintPlatform: StateFlow<List<String>>
//    val updateManagerState: StateFlow<UpdateState>
    val checkedPrintPlatformAll: MutableSharedFlow<Boolean>

    val printSingleFlow: MutableSharedFlow<ShopPrintOrderDetail>

    val loading: MutableStateFlow<Boolean>


    fun refreshPrintPlatform()
    fun toLoginPage()
    fun saveCurrentCheckedPrinter (printName: String)
    fun onChangeCheckedPrintPlatform(wmId: String)
   //  fun updateManagerDownLoad(url: String)

    fun onClickPrintTest(currentCheckedPrinterDevice: PrinterTarget?)

    fun onChangePrintPlatformAll(isAll: Boolean, platformIds: List<String>)

    fun onChangeShopAction()

    fun printSingleDoc(shopId: String, wmId: String, daySeq: String)


}

class DefaultHomeComponent (
    componentContext: ComponentContext,
    val toLogin: () -> Unit,
): HomeComponent, ComponentContext by componentContext {

    val currentDate: LocalDate = LocalDate.now()
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val currentDateFormat: String = currentDate.format(formatter)

    override val loading = MutableStateFlow(false)

    private val scope = componentContext.componentScope()
    // 当前门店ID
    override val currentShopId = DataStored.shopIdFlow.filter { it.isNotEmpty() }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    override val callPrintPlatform = MutableSharedFlow<Unit>(1)

    override val printSingleFlow = MutableSharedFlow<ShopPrintOrderDetail>(1)

    override val checkedPrintPlatformAll = MutableStateFlow(false)

    override val checkedPrintPlatform: StateFlow<List<String>> = DataStored.checkedPlatformFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val printPlatform: StateFlow<UiState<RequestResult<List<PrintPlatform>>>> = callPrintPlatform.flatMapLatest {
        AppRequest.safeRequestFlow {
            AppRequest.client.get("getPlatformList").body<RequestResult<List<PrintPlatform>>>()
        }
    }
    .onEach {
        val message = when (it) {
            is UiState.Success -> "获取平台列表成功！"
            is UiState.Error -> "获取平台列表失败！"
            else -> null
        }
        message?.let { msg ->
           HistoryLog.updateData(msg) // suspend-safe 调用
        }
    }.flowOn(Dispatchers.IO).flowOn(Dispatchers.IO).stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Idle
    )

    override val printPlatformIds = printPlatform.map {
        when(it) {
            is UiState.Success<RequestResult<List<PrintPlatform>>> -> {
                it.data.data?.map { item -> item.id }.orEmpty()
            }
            else -> {
                emptyList()
            }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    //
//    override val updateManagerState = UpdateManager.state.stateIn(
//        scope,
//        SharingStarted.Eagerly,
//        UpdateState.Idle
//    )

//    override fun updateManagerDownLoad(url: String) {
//        scope.launch {
//            UpdateManager.startUpdate(url)
//        }
//    }


    override fun saveCurrentCheckedPrinter (printName: String) {
        scope.launch(Dispatchers.IO) {
            DataStored.saveCurrentCheckedPrinter(printName)
        }
    }

    override fun onChangeCheckedPrintPlatform(wmId: String) {
        scope.launch(Dispatchers.IO) {
            DataStored.saveCheckedPlatform(wmId)
        }
    }

    override fun refreshPrintPlatform() {
        callPrintPlatform.tryEmit(Unit)
    }

    override fun toLoginPage() {
        toLogin()
    }

    override fun onClickPrintTest(currentCheckedPrinterDevice: PrinterTarget?) {
        currentCheckedPrinterDevice?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    PrinterManager.print(it, printImage2())
                } catch (err: Throwable) {
                    print(err.message)
                }
            }
        }
    }
    // 获取打印设备

    override fun onChangePrintPlatformAll(isAll: Boolean, platformIds: List<String>) {
        checkedPrintPlatformAll.value = isAll
        scope.launch {
            DataStored.saveDataToPlatform(platformIds)
        }
    }

    override fun onChangeShopAction() {
        loading.value = true
        scope.launch {
            PrintTask.stopPollingTask()
            DataStored.saveShopId("")
            onChangePrintPlatformAll(false, emptyList())
            PrintTask.clearPrintedOrderIds()
            HistoryLog.clearData()
            NetworkCheck.stopKeepCheck()
            withContext(Dispatchers.Main) {
                delay(1000)
                loading.value = false
                toLogin()
            }
        }
    }

    override fun printSingleDoc(shopId: String,  wmId: String, daySeq: String) {
        scope.launch {
            val printData =  withContext(Dispatchers.IO) {
                runCatching {
                    AppRequest.client.post("getOrder") {
                        setBody(
                            FormDataContent(
                                Parameters.build {
                                    append("wmid", wmId)
                                    append("shopid", shopId)
                                    append("day_seq", daySeq)
                                    append("secret", "panyishigedashuaige")

                                }
                            )
                        )
                    }.body<RequestResult<ShopPrintOrderDetail>>()
                }.getOrNull()
            }

            printData?.let {
                AppToast.showToast(it.msg)
            }

            printData?.data?.let {
                printSingleFlow.emit(it)
            }
            
        }
    }

}