package com.pgprint.app.utils

import com.pgprint.app.model.NetworkException
import com.pgprint.app.model.RequestResult
import com.pgprint.app.model.UiState
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

object AppRequest {

     val client = HttpClient(OkHttp) {

        expectSuccess = false

         defaultRequest {
             url("http://39.98.37.44/index.php/Home/WmPrintLee/")
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    explicitNulls = false
                },
                ContentType.Any
            )
        }

        // 2. 超时配置（桌面端网络环境通常较好，但要防范死连接）
        install(HttpTimeout) {
            requestTimeoutMillis = Long.MAX_VALUE
            connectTimeoutMillis = 600000
            socketTimeoutMillis = 150000
        }

        // 3. 日志（配合你之前的 SLF4J/Logback）
        install(Logging) {
            logger =  Logger.DEFAULT
            level = LogLevel.ALL
        }

        engine {
            // OkHttp 特有配置：可以设置代理
            config {
                followRedirects(true)
            }
        }

         HttpResponseValidator {
             validateResponse { response ->
                 val statusCode = response.status.value
                 if (statusCode >= 500) {
                     throw ServerResponseException(response, "服务端异常: $statusCode")
                 }
                 if (statusCode >= 400) {
                     throw ClientRequestException(response, "异常错误: $statusCode")
                 }
                 return@validateResponse
             }
             handleResponseExceptionWithRequest { cause, _ ->
                 val mapped = when (cause) {
                     is ClientRequestException -> {
                         val response = cause.response
                         ClientRequestException(response," ${cause.response.status.value}:${cause.message}")
                     }
                     is ServerResponseException -> {
                         val response = cause.response
                         ServerResponseException(response," ${cause.response.status.value}:${cause.message}")
                     }
                     is IOException -> NetworkException.NetworkUnavailable()
                     is TimeoutCancellationException -> NetworkException.Timeout()
                     else -> NetworkException.Unknown(cause)
                 }
                 throw mapped
             }
         }
    }

    fun <T> safeRequestFlow(
        block: suspend () -> RequestResult<T>
    ): Flow<UiState<RequestResult<T>>> = flow {
        val res = block()
        if (res.code == 0 || (res.code in 200..299)) {
            emit(UiState.Success(res))
        } else {
            emit(UiState.Error(res.msg))
        }
    }.onStart {
        emit(UiState.Loading)
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(UiState.Error(e.message ?: "服务端/网络异常"))
    }
}