package com.pgprint.app.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pgprint.app.model.PrintPlatform
import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.UiState
import com.pgprint.app.utils.AppColors


@Composable
fun HomeHeader(
    modifier: Modifier = Modifier,
    content: @Composable  (RowScope.() -> Unit)
) {

    Row (
        modifier = modifier.background(AppColors.HeaderBackground).padding(20.dp).horizontalScroll(
            state = rememberScrollState()
        ),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        content()
    }
}


@Composable
fun ChoosePrintDeviceList(
    modifier: Modifier = Modifier,
    printPlatformState: UiState<RequestResult<List<PrintPlatform>>>,
    checkedPrintPlatform: List<String>,
    onChangeCheckedPrintPlatform: (wmId: String) -> Unit,
) {
    Box(
        modifier = modifier.background(AppColors.WindowBackground).fillMaxSize(),
    ) {
        Crossfade(
            targetState = printPlatformState,
            modifier = Modifier.fillMaxSize(),
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
                    printPlatform.data?.let { list ->
                        val listState = rememberLazyListState()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        ) {
                            items(list,  key = { it.id }) { x ->
                                ChoosePlatformItem(
                                    platformTitle = x.label,
                                    platformId = x.id,
                                    platformImg = x.img,
                                    checked = checkedPrintPlatform.contains(x.id),
                                    onChange = onChangeCheckedPrintPlatform,
                                )
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = listState
                            )
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun ChoosePlatformItem(
    modifier: Modifier = Modifier,
    platformTitle: String = "",
    platformImg: String = "",
    platformId: String = "",
    checked: Boolean = true,
    onChange: (wmId: String) -> Unit,
) {
    CellItem(
        modifier = modifier,
        headlineContent = { Text(platformTitle, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = {
            AsyncImage(
                model = platformImg,
                contentDescription = "brand image",
                modifier = Modifier.size(20.dp)
            )
        },
        trailingContent = {
            Checkbox(
                modifier = Modifier.size(12.dp),
                checked = checked,
                onCheckedChange = {
                    onChange(platformId)
                }
            )
        },
        onClick = {
            onChange(platformId)
        }
    )
}




@Composable
fun ToolItem(
    modifier: Modifier = Modifier,
    title: String = "",
    titleSuffix: @Composable (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card (
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 16.dp)) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (title.isNotEmpty()) {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                titleSuffix?.invoke()
            }
            Spacer(Modifier.fillMaxWidth().height(10.dp))
            content()
        }
    }
}

