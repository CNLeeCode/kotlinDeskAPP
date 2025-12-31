package com.pgprint.app.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.refresh


@Composable
fun RefreshButton(
    modifier: Modifier = Modifier,
    containerSize: Dp = 32.dp,
    iconSize: Dp = 16.dp,
    containerPadding: PaddingValues = PaddingValues(4.dp),
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(CircleShape)
            .clickable {
                onClick()
            }
            .padding(containerPadding),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.refresh),
            contentDescription = "refresh",
            modifier = Modifier.size(iconSize)
        )
    }
}