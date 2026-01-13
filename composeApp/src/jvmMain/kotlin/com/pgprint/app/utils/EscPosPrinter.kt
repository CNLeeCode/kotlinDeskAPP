package com.pgprint.app.utils
import com.github.anastaciocintra.escpos.EscPos
import com.github.anastaciocintra.escpos.EscPosConst
import com.github.anastaciocintra.escpos.Style
import com.github.anastaciocintra.escpos.barcode.BarCode
import com.github.anastaciocintra.escpos.barcode.QRCode
import com.github.anastaciocintra.escpos.image.BitImageWrapper
import com.github.anastaciocintra.escpos.image.BitonalThreshold
import com.github.anastaciocintra.escpos.image.CoffeeImage
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl
import com.github.anastaciocintra.escpos.image.EscPosImage
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper
import com.pgprint.app.utils.Utils.generateCode128BarcodeImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.toBitmap
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import java.nio.charset.Charset
import javax.imageio.ImageIO

class EscPosPrinter(
    output: OutputStream,
    paperWidth: Int = WIDTH_58
) {

    companion object {
        const val WIDTH_58 = 32
        const val WIDTH_80 = 48
        private val GBK: Charset = charset("GBK")
    }

    private val escpos = EscPos(output)
    private val width = paperWidth

    /* ========== 基础 ========= */

    fun init() {
        escpos.write(byteArrayOf(0x1B, 0x40), 0, 2) // ESC @
    }

    fun feed(lines: Int = 1) {
        escpos.feed(lines)
    }

    fun cut() {
        escpos.cut(EscPos.CutMode.FULL)
    }

    fun close() {
        escpos.close()
    }

    /* ========== 文本 ========= */

    private fun byteWidth(text: String): Int =
        text.toByteArray(GBK).size

    fun writeText(text: String, style: Style = Style() ) {
        val bytes = text.toByteArray(GBK)
        escpos.write(bytes, 0, bytes.size).setStyle(style)
    }

    fun divider() {
        writeText("-".repeat(width) + "\n")
    }

    /* ========== 对齐 ========= */

    fun left() {
        escpos.write(byteArrayOf(0x1B, 0x61, 0x00), 0, 3)
    }

    fun center() {
        escpos.write(byteArrayOf(0x1B, 0x61, 0x01), 0, 3)
    }

    fun right() {
        escpos.write(byteArrayOf(0x1B, 0x61, 0x02), 0, 3)
    }

    /* ========== 字体 ========= */

    fun bold(on: Boolean = true) {
        escpos.write(byteArrayOf(0x1B, 0x45, if (on) 0x01 else 0x00), 0, 3)
    }

    fun doubleSize(on: Boolean = true) {
        escpos.write(byteArrayOf(0x1D, 0x21, if (on) 0x11 else 0x00), 0, 3)
    }

    fun scaleTextSize(width: Int, height: Int) {
        val n = ((width - 1) shl 4) or (height - 1)
        escpos.write(byteArrayOf(0x1D, 0x21, n.toByte()), 0, 3)
    }
    /* ========== 左右对齐 ========= */

    fun lineLR(left: String, right: String) {
        val space = width - byteWidth(left) - byteWidth(right)
        writeText(left + " ".repeat(space.coerceAtLeast(1)) + right + "\n")
    }

    /* ========== 商品（自动换行） ========= */

    private fun splitByWidth(text: String, maxWidth: Int): List<String> {
        val result = mutableListOf<String>()
        val buf = StringBuilder()
        var currentWidth = 0

        for (c in text) {
            val w = byteWidth(c.toString())
            if (currentWidth + w > maxWidth) {
                result.add(buf.toString())
                buf.clear()
                currentWidth = 0
            }
            buf.append(c)
            currentWidth += w
        }
        if (buf.isNotEmpty()) result.add(buf.toString())
        return result
    }

    fun product(
        name: String,
        qty: String,
        total: String
    ) {
        val rightText = "$qty  $total"
        val rightWidth = byteWidth(rightText)
        val nameWidth = width - rightWidth - 1

        val lines = splitByWidth(name, nameWidth)

        lines.forEachIndexed { index, line ->
            if (index == lines.lastIndex) {
                val space = width - byteWidth(line) - rightWidth
                writeText(line + " ".repeat(space) + rightText + "\n")
            } else {
                writeText(line + "\n")
            }
        }
    }

    /* ========== 条形码 ========= */

    fun barcode(data: String) {
        feed(1)
        val barcodeImage = generateCode128BarcodeImage(data)
        // 假设 barcodeImage 是你 ZXing 生成的 BufferedImage
        val coffeeImage = CoffeeImageImpl(barcodeImage)
        // 选择二值化算法（简单阈值最常用）
        val bitonalAlgorithm = BitonalThreshold()
        // 构造 EscPosImage
        val escposImage = EscPosImage(coffeeImage, bitonalAlgorithm)
        // 选择打印用的 wrapper（比如 RasterBitImageWrapper 支持较快 raster 方式打印）
        val imageWrapper = RasterBitImageWrapper()
        escpos.write(imageWrapper, escposImage)
    }

      suspend fun localImage(file: File) {
        fileToBufferedImage(file)?.let {
            // 构造 EscPosImage
            val escposImage = withContext(Dispatchers.Default) {
                val scaleImage = Utils.fileToBufferedImageWithFourMargin(it, 380, 380, 20, 20, 2, 10)
                val coffeeImage =  CoffeeImageImpl(scaleImage)
                // 选择二值化算法（简单阈值最常用）
                val bitonalAlgorithm = BitonalThreshold()
                EscPosImage(coffeeImage, bitonalAlgorithm)
            }
            // 选择打印用的 wrapper（比如 RasterBitImageWrapper 支持较快 raster 方式打印）
            val imageWrapper = RasterBitImageWrapper()
            escpos.write(imageWrapper, escposImage)
        }
    }

    suspend fun localImage2(file: File) {
        fileToBufferedImage(file)?.let {
            // 构造 EscPosImage
            val escposImage = withContext(Dispatchers.Default) {
                val scaleImage = Utils.fileToBufferedImageWithFourMargin(it, 380, 80, 0, 0, 0, 0)
                val coffeeImage =  CoffeeImageImpl(scaleImage)
                // 选择二值化算法（简单阈值最常用）
                val bitonalAlgorithm = BitonalThreshold()
                EscPosImage(coffeeImage, bitonalAlgorithm)
            }
            // 选择打印用的 wrapper（比如 RasterBitImageWrapper 支持较快 raster 方式打印）
            val imageWrapper = RasterBitImageWrapper()
            escpos.write(imageWrapper, escposImage)
        }
    }

    /* ========== 二维码 ========= */

    fun qrcode(data: String, size: Int = 8) {
        feed(1)
        val qr = QRCode()
        qr.setSize(size)
        qr.setJustification(EscPosConst.Justification.Center)
        escpos.write(qr, data)
        feed(1)
    }

     fun fileToBufferedImage(file: File): BufferedImage? {
        if (file.exists()) {
            return ImageIO.read(file)
        }
       return null
    }


    fun resizeBufferedImage(
        src: BufferedImage,
        canvasW: Int,
        canvasH: Int
    ): BufferedImage {
        val canvas = BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB)
        val g2d = canvas.createGraphics()

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // 背景填充白色（方便打印机识别）
        g2d.fillRect(0, 0, canvasW, canvasH)

        val scale = minOf(canvasW.toDouble() / src.width, canvasH.toDouble() / src.height)
        val drawW = (src.width * scale).toInt()
        val drawH = (src.height * scale).toInt()

        val x = (canvasW - drawW) / 2
        val y = (canvasH - drawH) / 2

        g2d.drawImage(src, x, y, drawW, drawH, null)
        g2d.dispose()

        return canvas
    }
}
