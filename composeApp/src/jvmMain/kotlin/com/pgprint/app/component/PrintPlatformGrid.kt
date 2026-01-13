package com.pgprint.app.component

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.model.PrintPlatform
import com.pgprint.app.model.ShopPrintOrderItem
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.AppToast
import com.pgprint.app.utils.Utils
import kotlinx.coroutines.launch

@Composable
fun PrintPlatformGrid(
    modifier: Modifier = Modifier,
    printPlatformList: List<PrintPlatform>,
    printedOrderMapList: Map<String, Map<String, ShopPrintOrderItem>>,
    shopId: String,
    onPrintDoc: (shopId: String, wmId: String, daySeq: String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.FixedSize(size = 300.dp),
        modifier = modifier.padding(10.dp).background(Color.White),
        state = rememberLazyGridState(),
        contentPadding =  PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false
    ) {
        items(printPlatformList,  key = { it.id }) {
            PrintPlatformGridItem(
                platformName = it.label,
                platformId = it.id,
                printedOrderMapList = printedOrderMapList,
                shopId = shopId,
                onPrintDoc = onPrintDoc,
            )
        }
    }
}

// 拷贝到粘贴板
//  val selection = StringSelection(it.orderId)
//  Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)

@Composable
fun PrintPlatformGridItem(
    modifier: Modifier = Modifier,
    platformName: String,
    platformId: String,
    shopId: String,
    printedOrderMapList: Map<String, Map<String, ShopPrintOrderItem>>,
    onPrintDoc: (shopId: String, wmId: String, daySeq: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val textModifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color.White).padding(vertical = 10.dp, horizontal = 16.dp)

    val platformPrintedList by remember(platformId, printedOrderMapList) {
        derivedStateOf {
            printedOrderMapList[platformId]
                ?.values
                ?.sortedBy{ it.getKey() }
                .orEmpty()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().height(400.dp).border(1.dp, color = AppColors.HeaderBackground).background(AppColors.HeaderBackground, RoundedCornerShape(2.dp))
    ) {
        Row (
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 16.dp).padding(top=4.dp)
        ) {
            Text("${platformName}(${platformPrintedList.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.padding(horizontal = 5.dp).padding(bottom = 5.dp).fillMaxWidth().weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(5.dp).border(1.dp, color = AppColors.HeaderBackground),
                state = lazyListState
            ) {
                items(platformPrintedList, key = { it.orderId }) {
                    Row (
                        modifier = textModifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("#${it.daySeq}", fontSize = 16.sp)
                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            PrintButton(
                                btnText = "重打",
                                onClick = remember(it.orderId, platformId, shopId) {
                                    {
                                        onPrintDoc(shopId, platformId, it.orderId)
                                    }
                                }
                            )

                            PrintButton(
                                btnText = "复单",
                                onClick = remember(it.orderId) {
                                    {
                                        scope.launch {
                                            AppToast.showToast("复制成功：${it.orderId}")
                                        }
                                        Utils.copyToClipboard(it.orderId)
                                    }
                                }
                            )
                        }

                    }
                    HorizontalDivider()
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
}


@Composable
fun PrintButton(
    modifier: Modifier = Modifier,
    btnText: String = "",
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.clickable(
            onClick = onClick
        ).border(1.dp, AppColors.PrimaryColor, RoundedCornerShape(5.dp)).clip(RoundedCornerShape(5.dp)).padding(vertical = 5.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = btnText, fontSize = 14.sp, color = AppColors.PrimaryColor)
    }
}