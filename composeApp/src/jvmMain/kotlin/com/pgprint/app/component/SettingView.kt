package com.pgprint.app.component

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.model.PrinterTarget
import com.pgprint.app.utils.DesktopAudioPlayer
import com.pgprint.app.utils.DesktopTool
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Close_circle_fill
import pgprint.composeapp.generated.resources.Res


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingView(
    modifier: Modifier = Modifier,
    onClickPrintTest: () -> Unit = {},
    onClickOpenDrawer: () -> Unit = {}
) {

    val scope = rememberCoroutineScope()
    var showKFPhotoPopup by remember {
        mutableStateOf(false)
    }

    val lazyListState = rememberLazyListState()

    if (showKFPhotoPopup) {
        BasicAlertDialog(
            onDismissRequest = {
                showKFPhotoPopup = false
            },
            modifier = Modifier.size(800.dp, 600.dp).padding(10.dp).clip(RoundedCornerShape(5.dp)).background(
                Color.White
            )
        ) {
            Column {
                Row (
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "设置客服二维码",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Icon(
                        painter = painterResource(Res.drawable.Close_circle_fill),
                        modifier = Modifier.size(22.dp).clickable {
                            showKFPhotoPopup = false
                        },
                        contentDescription = "Close",
                    )
                }
                DragAndClickDropZone(
                    modifier = Modifier
                )
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            item (key = "9999") {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        DesktopTool.openBrowser("http://wm.butsdgc.com/index.php/Home/MgTest/")
                    },
                    shape = CutCornerShape(2.dp),
                ) {
                    Text("管理后台")
                }
            }
            item (key = "0000") {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClickPrintTest,
                    shape = CutCornerShape(2.dp),
                ) {
                    Text("打印测试")
                }
            }

            item(key = "0001") {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showKFPhotoPopup = true
                    },
                    shape = CutCornerShape(2.dp),
                ) {
                    Text("设置客服二维码")
                }
            }
            item (key = "0002") {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            val url = ClassLoader.getSystemResource("notice.wav")
                            DesktopAudioPlayer.play2(url, true)
                        }
                    },
                    shape = CutCornerShape(2.dp),
                ) {
                    Text("播放音频")
                }
            }
            item (key = "0003") {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClickOpenDrawer,
                    shape = CutCornerShape(2.dp),
                ) {
                    Text("查询打印")
                }
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