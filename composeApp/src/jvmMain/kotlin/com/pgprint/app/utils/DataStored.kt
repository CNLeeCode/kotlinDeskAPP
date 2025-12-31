package com.pgprint.app.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pgprint.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    val shopIdFlow : Flow<String> = stored.data.map {
        it[SHOP_ID] ?: ""
    }

    suspend fun saveShopId(shopId: String) {
        stored.edit { it ->
            it[SHOP_ID] = shopId
        }
    }
}