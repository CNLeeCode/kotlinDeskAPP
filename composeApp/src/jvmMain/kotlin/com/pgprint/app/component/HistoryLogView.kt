package com.pgprint.app.component

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgprint.app.model.ConnectionInfo
import com.pgprint.app.utils.AppColors

@Composable
fun HistoryLogView(
    modifier: Modifier = Modifier,
    historyLog: List<ConnectionInfo> = emptyList(),
) {
    val state = rememberLazyListState()

    LaunchedEffect(historyLog) {
        state.animateScrollToItem(0)
    }
    
    Box(
        modifier = modifier.fillMaxSize().background( color =  AppColors.WindowBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state
        ) {
           items(items = historyLog,  key = { it.key }) {
               Text(text = it.message, fontSize = 14.sp, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(4.dp))
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