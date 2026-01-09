package com.pgprint.app.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(500.dp).fillMaxHeight().background(Color.White).padding(10.dp)
    ) {
        Text("这里是 DrawerContent")
    }
}