package com.pgprint.app.utils

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
}
