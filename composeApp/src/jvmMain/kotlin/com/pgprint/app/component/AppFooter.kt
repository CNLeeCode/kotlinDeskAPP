package com.pgprint.app.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AppFooter(
    modifier: Modifier = Modifier,
    text: String,
) {
    Row (
        modifier = modifier.background(Color.White).padding(10.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}