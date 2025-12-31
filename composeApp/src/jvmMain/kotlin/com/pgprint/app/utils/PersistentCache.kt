package com.pgprint.app.utils

import com.pgprint.app.BuildConfig
import org.ehcache.Cache
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import java.io.File


object PersistentCache {
    private val STORED_DIR = BuildConfig.STORED_DIR

    private val cacheDir: File = run {
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

    private val manager by lazy {
        CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(cacheDir))
            .build(true)
    }

    val configCache: Cache<String, String> by lazy {
        manager.createCache(
            "configCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String::class.java,
                String::class.java,
                ResourcePoolsBuilder.heap(100)
                    .disk(20, MemoryUnit.MB, true)
            )
        )
    }
}