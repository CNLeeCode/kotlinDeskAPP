package com.pgprint.app.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.awt.image.BufferedImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.swing.SwingUtilities


object Utils {

    internal fun <T> runOnUiThread(block: () -> T): T {
        if (SwingUtilities.isEventDispatchThread()) {
            return block()
        }
        var error: Throwable? = null
        var result: T? = null

        SwingUtilities.invokeAndWait {
            try {
                result = block()
            } catch (e: Throwable) {
                error = e
            }
        }
        error?.also { throw it }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    fun formatTimestamp(
        timestampMillis: Long,
    ): String {
        // timestamp 是 10 位秒级时间戳
        val instant = Instant.ofEpochSecond(timestampMillis)
        val zone = ZoneId.systemDefault()

        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CHINA) // a = 上午/下午, hh = 12小时制
        return instant.atZone(zone).format(formatter)
    }

    fun generateCode128BarcodeImage(data: String, width: Int = 384, height: Int = 100, bottomMargin: Int = 10): BufferedImage {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.CODE_128, width, height)

        // 新图高度 = 原高度 + 下间距
        val totalHeight = height + bottomMargin
        val image = BufferedImage(width, totalHeight, BufferedImage.TYPE_BYTE_BINARY)

        // 填充整张图为白色
        for (x in 0 until width) {
            for (y in 0 until totalHeight) {
                image.setRGB(x, y, 0xFFFFFF) // 白色背景
            }
        }

        // 将条码绘制到图像顶部
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (bitMatrix.get(x, y)) 0x000000 else 0xFFFFFF
                image.setRGB(x, y, color)
            }
        }
        return image
    }

    fun hasMiddleSpace(str: String): Boolean {
        return Regex("\\S\\s+\\S").containsMatchIn(str)
    }
}
