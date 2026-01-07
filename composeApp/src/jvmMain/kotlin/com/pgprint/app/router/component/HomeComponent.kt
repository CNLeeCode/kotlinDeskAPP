package com.pgprint.app.router.component

import androidx.compose.runtime.remember
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.arkivanov.decompose.ComponentContext
import com.pgprint.app.componentScope
import com.pgprint.app.model.ConnectionInfo
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.model.PrintPlatform
import com.pgprint.app.model.PrinterTarget
import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.UiState
import com.pgprint.app.model.UpdateState
import com.pgprint.app.utils.AppRequest
import com.pgprint.app.utils.DataStored
import com.pgprint.app.utils.DatabaseManager
import com.pgprint.app.utils.UpdateManager
import com.pgprint.app.utils.UsbDevices
import com.pgprint.app.utils.Utils
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


interface HomeComponent {
    val currentShopId: StateFlow<String>
    val callPrintPlatform: MutableSharedFlow<Unit>
    val printPlatform: StateFlow<UiState<RequestResult<List<PrintPlatform>>>>
    val checkedPrintPlatform: StateFlow<List<String>>
    val HistoryLog: StateFlow<List<ConnectionInfo>>

    val updateManagerState: StateFlow<UpdateState>

    fun refreshPrintPlatform()
    fun toLoginPage()
    fun saveCurrentCheckedPrinter (printName: String)
    fun onChangeCheckedPrintPlatform(wmId: String)
    fun updateManagerDownLoad(url: String)
}

class DefaultHomeComponent (
    componentContext: ComponentContext,
    shopId: String,
    val toLogin: () -> Unit,
): HomeComponent, ComponentContext by componentContext {

    val currentDate: LocalDate = LocalDate.now()
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val currentDateFormat: String = currentDate.format(formatter)
    private val scope = componentContext.componentScope()
    // 当前门店ID
    override val currentShopId = MutableStateFlow(shopId)

    override val callPrintPlatform = MutableSharedFlow<Unit>(1)

    override val checkedPrintPlatform: StateFlow<List<String>> = DataStored.checkedPlatformFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    override val HistoryLog: StateFlow<List<ConnectionInfo>> = DatabaseManager.database.connectionInfoQueries.selectByDate(currentDateFormat).asFlow().mapToList(
        Dispatchers.IO).map {
        it.map { info ->
            ConnectionInfo(
                key =  "${info.id}-${info.createdAt}",
                message =  Utils.formatTimestamp(info.createdAt) + " " + info.connectionDetail,
                time = info.createdAt
            )
        }
    }.stateIn(
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
            insertNewToConnectionInfo(msg) // suspend-safe 调用
        }
    }.flowOn(Dispatchers.IO).flowOn(Dispatchers.IO).stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Idle
    )

    //
    override val updateManagerState = UpdateManager.state.stateIn(
        scope,
        SharingStarted.Eagerly,
        UpdateState.Idle
    )

    override fun updateManagerDownLoad(url: String) {
        scope.launch {
            UpdateManager.startUpdate(url)
        }
    }


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


    suspend fun insertNewToConnectionInfo(message: String, color: String = "#07c160") {
        try {
            withContext(Dispatchers.IO.limitedParallelism(1)) {
                DatabaseManager.database.connectionInfoQueries.insertConnection(
                    dateText = currentDateFormat,
                    createdAt = System.currentTimeMillis() / 1000,
                    connectionDetail = message,
                    textColor = color,
                )
            }
        } catch (err: Throwable) {
            println("insertNewToConnectionInfo Error ${err.message}")
        }
    }

    override fun refreshPrintPlatform() {
        callPrintPlatform.tryEmit(Unit)
    }

    override fun toLoginPage() {
        toLogin()
    }
    // 获取打印设备

}