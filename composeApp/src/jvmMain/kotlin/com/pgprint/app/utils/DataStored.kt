package com.pgprint.app.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.Path.Companion.toOkioPath
import java.io.File


fun provideDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val dataStoreFile = File(PersistentCache.cacheDir, "app_prefs.preferences_pb")
            // 确保父目录存在
            dataStoreFile.parentFile?.mkdirs()
            dataStoreFile.toOkioPath()
        }
    )
}

object DataStored  {
    val stored by lazy {
        provideDataStore()
    }

    val SHOP_ID = stringPreferencesKey("shopid")
    val CHECKED_PRINT_PLATFORM = stringPreferencesKey("checked_platform")
    val CHECKED_PRINTER_NAME = stringPreferencesKey("checked_printer_name")

    val shopIdFlow : Flow<String> = stored.data.map {
        it[SHOP_ID] ?: ""
    }

    val checkedPlatformFlow: Flow<List<String>> =  stored.data.map { prefs ->
        prefs[CHECKED_PRINT_PLATFORM]
            ?.let { Json.decodeFromString<List<String>>(it) }
            ?: emptyList()
    }

    val currentCheckedPrinter: Flow<String> = stored.data.map { prefs ->
        prefs[CHECKED_PRINTER_NAME] ?: ""
    }

    suspend fun saveShopId(shopId: String) {
        stored.edit {
            it[SHOP_ID] = shopId
        }
    }

    suspend fun saveCurrentCheckedPrinter(printName: String) {
        withContext(Dispatchers.IO) {
            stored.edit { prefs ->
                if (prefs[CHECKED_PRINTER_NAME] == printName) {
                    prefs[CHECKED_PRINTER_NAME] = ""
                } else {
                    prefs[CHECKED_PRINTER_NAME] = printName
                }
            }
        }
    }

    suspend fun saveCheckedPlatform(wmId: String) {
        withContext(Dispatchers.IO) {
            stored.edit { prefs ->
                val currentList =
                    prefs[CHECKED_PRINT_PLATFORM]
                        ?.let { Json.decodeFromString<List<String>>(it) }
                        ?: emptyList()
                val newList =
                    if (wmId in currentList) {
                        currentList - wmId   // 已存在 → 移除
                    } else {
                        currentList + wmId   // 不存在 → 添加
                    }
                prefs[CHECKED_PRINT_PLATFORM] =
                    Json.encodeToString(newList)
            }
        }
    }

    suspend fun saveDataToPlatform(plateList: List<String>) {
        withContext(Dispatchers.IO) {
            stored.edit { prefs ->
                prefs[CHECKED_PRINT_PLATFORM] =  Json.encodeToString(plateList)
            }
        }
    }
}