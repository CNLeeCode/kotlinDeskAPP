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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.model.PrintDeviceData
import com.pgprint.app.utils.AppColors
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.usb

@Composable
fun UsbView(
    modifier: Modifier = Modifier,
    state: PrintDeviceData
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
                val state = rememberLazyListState()

                Box(
                    modifier = Modifier.fillMaxSize().background( color =  AppColors.WindowBackground)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = state
                    ) {
                        items(10) { x ->
                            ChoosePrinterDeviceItem()
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(
                            scrollState = state
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
    name: String = "打印机名称打印机名称打印机名称打印机名称",
    desc: String = "类型"
) {
    CellItem(
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
                checked = false,
                onCheckedChange = {
                    println("onCheckedChange $it")
                },
                modifier = Modifier.size(12.dp)
            )
        },
    )
}