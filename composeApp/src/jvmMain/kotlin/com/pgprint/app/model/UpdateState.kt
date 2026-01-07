package com.pgprint.app.model

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Downloading(
        val progress: Float,
        val downloaded: Long,
        val total: Long
    ) : UpdateState

    data object Installing : UpdateState
    data object Finished : UpdateState
    data class Error(val message: String) : UpdateState
}
