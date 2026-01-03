package com.pgprint.app.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pgprint.app.utils.AppColors

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit ),
    leadingContent: (@Composable () -> Unit ),
) {
   Column(
       modifier = modifier
   ) {
       Row (
           modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 10.dp, horizontal = 20.dp),
           verticalAlignment = Alignment.CenterVertically,
           horizontalArrangement = Arrangement.SpaceBetween
       ) {
           leadingContent()
           trailingContent()
       }
       VerticalDivider(
           modifier = Modifier.fillMaxWidth(),
           color = AppColors.WindowBackground,
           thickness = 1.dp
       )
   }
}