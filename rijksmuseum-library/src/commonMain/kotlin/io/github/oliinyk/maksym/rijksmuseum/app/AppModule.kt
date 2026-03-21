package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.oliinyk.maksym.rijksmuseum.artwork.DetailsModule
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ValueHolder
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.SearchModule
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.RijksmuseumApiImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.Navigator
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
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind

@Suppress("FunctionName")
internal fun AppModule(
    backStack: NavBackStack<NavKey>,
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
): Module = module {
    includes(SearchModule, DetailsModule)
    single { HttpClient(LogLevel.ALL, engine) }
    single { Navigator(backStack, get(named<Artwork>())) }
    single(named<Artwork>()) { ValueHolder<Artwork>() }
    singleOf(::RijksmuseumApiImpl).bind(RijksmuseumApi::class)
}

private fun HttpClient(
    logLevel: LogLevel,
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
): HttpClient = HttpClient(engine) {
    expectSuccess = true

    headers {
        append("Accept", "application/json")
    }

    install(HttpTimeout) {
        // todo provide via build config
        requestTimeoutMillis = RequestTimeoutMillis
        connectTimeoutMillis = ConnectTimeoutMillis
        socketTimeoutMillis = SocketTimeoutMillis
    }

    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
                isLenient = true
            }
        )
    }

    Logging {
        logger = Logger.SIMPLE
        level = logLevel
    }
}

private const val RequestTimeoutMillis = 5000L
private const val ConnectTimeoutMillis = 5000L
private const val SocketTimeoutMillis = 7000L
