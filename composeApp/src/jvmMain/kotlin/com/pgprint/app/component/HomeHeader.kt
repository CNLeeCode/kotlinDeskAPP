package com.pgprint.app.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.AppRequest
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.dy


@Composable
fun HomeHeader(
    modifier: Modifier = Modifier,
    content: @Composable  (RowScope.() -> Unit)
) {
    Row (
        modifier = modifier.background(AppColors.HeaderBackground).padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        content()
    }
}


@Composable
fun ChoosePrintDeviceList(
    modifier: Modifier = Modifier,
) {
    // val state = rememberLazyGridState()
    val state = rememberLazyListState()
    Box(
        modifier = modifier.background(AppColors.WindowBackground).fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state
        ) {
            items(10) { x ->
                ChoosePlatformItem(
                    modifier = Modifier.height(40.dp)
                )
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



@Composable
fun ChoosePlatformItem(
    modifier: Modifier = Modifier,
    title: String = "外卖平台外卖平台外卖平台外卖平台"
) {
    CellItem(
        modifier = modifier.fillMaxWidth().clickable {
            println("clickable")
        },
        headlineContent = { Text(title, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = {
            Image(
                painter = painterResource(Res.drawable.dy),
                contentDescription = "platform name",
                modifier = Modifier.size(20.dp)
            )
        },
        trailingContent = {
            Checkbox(
                modifier = Modifier.size(12.dp),
                checked = false,
                onCheckedChange = {
                    println("onCheckedChange $it")
                }
            )
        },
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
        modifier = modifier.fillMaxSize(),
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
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                titleSuffix?.invoke()
            }
            Spacer(Modifier.fillMaxWidth().height(10.dp))
            content()
        }
    }
}

