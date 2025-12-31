package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily


@Composable
fun getAppFontFamily() = FontFamily(
    // 使用新版 Compose Resources 生成的 Res 对象
    // Font(Res.font.maShanZhengRegular, FontWeight.Normal, FontStyle.Normal),
)
