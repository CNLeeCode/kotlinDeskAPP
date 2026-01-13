package com.pgprint.app.model

sealed interface AppVersionState {
    object Checking: AppVersionState
    data class Update(val data: AppVersion): AppVersionState
    object Usual: AppVersionState
    data class Error(val msg: String): AppVersionState
}
