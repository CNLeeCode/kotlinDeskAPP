package com.pgprint.app.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.pgprint.app.BuildConfig
import okio.Path.Companion.toOkioPath
import java.awt.Desktop
import java.io.File
import java.util.Properties

object PersistentCache {
    private val STORED_DIR = BuildConfig.STORED_DIR

    val cacheDir: File = run {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        val finalFolder = when {
            os.contains("win") -> {
                val rootDisk = File(userHome).toPath().root.toString()
                if (rootDisk.startsWith("C", ignoreCase = true)) {
                    File(userHome, "${STORED_DIR}/cache")
                } else {
                    File(rootDisk, "${STORED_DIR}/cache")
                }
            }
            os.contains("mac") -> File(userHome, "Library/Application Support/${STORED_DIR}/cache")
            else -> File(userHome, ".${STORED_DIR}/cache")
        }

        if (!finalFolder.exists()) finalFolder.mkdirs()
        finalFolder
    }

    fun openCatchDir() {
        Desktop.getDesktop().open(cacheDir)
    }
}
