package com.pgprint.app.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.swing.SwingUtilities
import com.pgprint.app.BuildConfig.DOMAIN_URL
import org.jetbrains.compose.resources.DrawableResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.mt
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

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

    fun generateCode128BarcodeImage(data: String, width: Int = 384, height: Int = 80, bottomMargin: Int = 10): BufferedImage {
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

    /**
     * 读取图片文件并生成指定大小的 BufferedImage，内容居中，四周留白可调
     *
     * @param file 图片文件
     * @param canvasWidth 最终 BufferedImage 宽度（例如打印机像素宽度）
     * @param canvasHeight 最终 BufferedImage 高度
     * @param margin 四周留白像素
     */
    fun fileToBufferedImageWithFourMargin(
        src: BufferedImage,
        canvasWidth: Int,
        canvasHeight: Int,
        marginLeft: Int = 0,
        marginRight: Int = 0,
        marginTop: Int = 0,
        marginBottom: Int = 0
    ): BufferedImage {

        val availableWidth = canvasWidth - marginLeft - marginRight
        val availableHeight = canvasHeight - marginTop - marginBottom

        // 等比缩放，但不超过可用宽高
        val scale = minOf(
            availableWidth.toDouble() / src.width,
            availableHeight.toDouble() / src.height,
            1.0 // 不放大
        )

        val drawW = (src.width * scale).toInt()
        val drawH = (src.height * scale).toInt()

        // 绘制位置严格在指定边距内
        val x = marginLeft + (availableWidth - drawW) / 2
        val y = marginTop + (availableHeight - drawH) / 2

        val canvas = BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = canvas.createGraphics()

        g2d.fillRect(0, 0, canvasWidth, canvasHeight)

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2d.drawImage(src, x, y, drawW, drawH, null)
        g2d.dispose()
        return canvas
    }

    fun hasMiddleSpace(str: String): Boolean {
        return Regex("\\S\\s+\\S").containsMatchIn(str)
    }

    fun getResourcesDir(): File {
        // 1. 获取程序运行时的路径
        val appPath = System.getProperty("compose.application.extraResources.dir")

        return if (appPath != null) {
            // 说明是打包后的安装环境
            File(appPath)
        } else {
            // 说明是开发环境 (IDE 运行)
            File(System.getProperty("user.dir") + "/extraResources")
        }
    }

    fun getAudioFile(fileName: String): File {
        return File(getResourcesDir(), "audio/$fileName")
    }

    fun deleteFile(file: File): Boolean {
        return try {
            Files.deleteIfExists(file.toPath())
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun compareVersion(v1: String, v2: String): Int {
        val p1 = v1.split(".")
        val p2 = v2.split(".")

        val max = maxOf(p1.size, p2.size)

        for (i in 0 until max) {
            val n1 = p1.getOrNull(i)?.toIntOrNull() ?: 0
            val n2 = p2.getOrNull(i)?.toIntOrNull() ?: 0

            if (n1 != n2) {
                return n1.compareTo(n2)
            }
        }
        return 0
    }

    fun openDownloadPage() {
        DesktopTool.openBrowser("${DOMAIN_URL}/index.php/Home/WmPrintLee/getDownloadPage")
    }

    fun openFAQPage() {
        DesktopTool.openBrowser("${DOMAIN_URL}/index.php/Home/WmPrintLee/faq")
    }

    fun copyToClipboard(text: String) {
        EventQueue.invokeLater {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(text), null)
        }
    }

}
