package com.pgprint.app.utils

import com.pgprint.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow



object AppFlow {

    val currentShopId = MutableStateFlow("")

    fun saveCurrentShopId(shopId: String) {
        currentShopId.value = shopId
        val key = BuildConfig.STORED_PREFX + "currentShopId"
    }

}