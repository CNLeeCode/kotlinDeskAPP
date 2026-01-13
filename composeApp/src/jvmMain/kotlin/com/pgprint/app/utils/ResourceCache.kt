package com.pgprint.app.utils

import java.awt.Desktop
import java.io.File
import java.net.URI

object ResourceCache {

    private val cacheDir: File by lazy {
        File(System.getProperty("java.io.tmpdir"), "my_app_resources").apply {
            mkdirs()
        }
    }

    private val lock = Any()

    fun getOrCreate(
        source: URI,
        fileName: String
    ): File {
        val target = File(cacheDir, fileName)

        if (target.exists() && target.length() > 0) {
            return target
        }

        synchronized(lock) {
            if (target.exists() && target.length() > 0) {
                return target
            }

            when (source.scheme) {
                "file" -> {
                    File(source).copyTo(target, overwrite = true)
                }

                "jar" -> {
                    source.toURL().openStream().use { input ->
                        target.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                "http", "https" -> {
                    source.toURL().openStream().use { input ->
                        target.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                else -> {
                    // fallback
                    source.toURL().openStream().use { input ->
                        target.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }

        return target
    }

    fun openCacheDir() {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(cacheDir)
        }
    }

    fun clearAll() {
        synchronized(lock) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
    }
}
