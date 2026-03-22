package io.github.oliinyk.maksym.rijksmuseum.app

import io.github.oliinyk.maksym.rijksmuseum.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun HttpClient(
    logLevel: LogLevel,
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
): HttpClient = HttpClient(engine) {
    expectSuccess = true

    headers {
        append("Accept", "application/json")
    }

    install(HttpTimeout) {
        requestTimeoutMillis = BuildConfig.RequestTimeoutMs
        connectTimeoutMillis = BuildConfig.ConnectTimeoutMs
        socketTimeoutMillis = BuildConfig.SocketTimeoutMs
    }

    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            }
        )
    }

    Logging {
        logger = Logger.SIMPLE
        level = logLevel
    }
}
