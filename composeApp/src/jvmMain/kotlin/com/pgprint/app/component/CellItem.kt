package com.pgprint.app.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

@Composable
fun CellItem(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(vertical = 8.dp, horizontal =  5.dp),
    headlineContent: @Composable (() -> Unit)? = null,
    overlineContent: @Composable (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit ) ? = null,
    leadingContent: (@Composable () -> Unit ) ? = null,
    onClick: () -> Unit = {},
) {
    Row (
        modifier = modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand).clickable(
            onClick = onClick
        ).padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        leadingContent?.let { view ->
            Box (
                modifier = Modifier.padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                view()
            }
        }
        Column (
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            headlineContent?.invoke()
            overlineContent?.invoke()
        }
        trailingContent?.let { view ->
            Box (
                modifier = Modifier.padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                view()
            }
        }

    }
}