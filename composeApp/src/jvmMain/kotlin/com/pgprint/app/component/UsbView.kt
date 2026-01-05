package com.pgprint.app.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.model.PrinterTarget
import com.pgprint.app.utils.AppColors
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.usb

@Composable
fun UsbView(
    modifier: Modifier = Modifier,
    state: PrintDeviceData,
    currentCheckedPrinter: String,
    onChangePrinter: (data: String) -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize().background(AppColors.WindowBackground),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is PrintDeviceData.Error -> {
                Text(
                    text = "异常：$state.message",
                    color = AppColors.ErrorRed,
                    fontSize = 16.sp
                )
            }
            is PrintDeviceData.Loading ->  {
                CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    color = AppColors.PrimaryColor,
                    modifier = Modifier.size(24.dp).offset(y = (-10).dp)
                )
            }
            is PrintDeviceData.Success -> {
                val lazyListState = rememberLazyListState()
                val printerDeviceList = state.data

                Box(
                    modifier = Modifier.fillMaxSize().background( color =  AppColors.WindowBackground)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState
                    ) {

                        items(items = printerDeviceList, key = { it.key }) { printer ->
                            ChoosePrinterDeviceItem(
                                name = printer.portName,
                                desc = when (printer) {
                                    is PrinterTarget.Driver ->  "系统"
                                    is PrinterTarget.Serial ->  "串口"
                                },
                                checked = printer.portName == currentCheckedPrinter,
                                onChange = onChangePrinter,
                            )
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(
                            scrollState = lazyListState
                        )
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ChoosePrinterDeviceItem(
    modifier: Modifier = Modifier,
    name: String = "设备名称",
    desc: String = "设备类型",
    checked: Boolean = false,
    onChange: (device: String) -> Unit
) {
    val currentOnChange by rememberUpdatedState(onChange)
    val onChecked = remember(name) {
        {
            currentOnChange(name)
        }
    }

    CellItem(
        modifier = modifier,
        onClick = onChecked,
        headlineContent  = {
            EllipsisTooltipText(
                text = name,
                fontSize = 14.sp,
            ) {
                Text(text = name, fontSize = 12.sp, color = Color.Gray)
            }
        },
        overlineContent = { Text(text =desc, fontSize = 10.sp, color = Color.Gray) },
        leadingContent = {
            Image(
                painter = painterResource(Res.drawable.usb),
                contentDescription = "platform name",
                modifier = Modifier.size(20.dp)
            )
        },
        trailingContent = {
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    onChecked()
                },
                modifier = Modifier.size(12.dp)
            )
        },
    )
}