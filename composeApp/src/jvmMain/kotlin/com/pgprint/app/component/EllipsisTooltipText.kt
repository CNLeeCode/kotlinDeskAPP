package com.pgprint.app.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EllipsisTooltipText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    plainTooltip: @Composable () -> Unit,
) {
    var isOverflow by remember { mutableStateOf(false) }
    val state = rememberTooltipState()
    val positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above)
    TooltipBox(
        tooltip = {
            if (isOverflow) {
                PlainTooltip(
                    containerColor = Color.White,
                    contentColor = Color.White,
                ) {
                    plainTooltip()
                }
            }
        },
        state = state,
        positionProvider = positionProvider
    ) {
        Text(
            text = text,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            modifier = modifier,
            onTextLayout = {
                isOverflow = it.hasVisualOverflow
            }
        )
    }
}
