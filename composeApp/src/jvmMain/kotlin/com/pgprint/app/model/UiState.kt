package com.pgprint.app.model

sealed interface UiState<out T> {
    object Idle: UiState<Nothing>
    object Loading: UiState<Nothing>
    data class Error(val message: String): UiState<Nothing>
    data class Success<T>(val data: T): UiState<T>
}