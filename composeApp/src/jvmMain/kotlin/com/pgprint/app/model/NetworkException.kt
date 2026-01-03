package com.pgprint.app.model

sealed class NetworkException(
    override val message: String?,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    class NetworkUnavailable : NetworkException("无法连接网络") {
        private fun readResolve(): Any = NetworkUnavailable()
    }

    class Timeout : NetworkException("请求超时") {
        private fun readResolve(): Any = Timeout()
    }

    data class Unknown(
        override val cause: Throwable?
    ) : NetworkException(cause?.message, cause)
}
