package com.pgprint.app.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object AppRequest {

     val client = HttpClient(OkHttp) {

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        // 2. 超时配置（桌面端网络环境通常较好，但要防范死连接）
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 15000
        }

        // 3. 日志（配合你之前的 SLF4J/Logback）
        install(Logging) {
            logger =  Logger.DEFAULT
            level = LogLevel.INFO
        }

        engine {
            // OkHttp 特有配置：可以设置代理
            config {
                followRedirects(true)
            }
        }
    }
}