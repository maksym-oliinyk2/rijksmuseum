package io.github.oliinyk.maksym.rijksmuseum.search.list

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind

internal val searchModule = module {
    // todo replace with db or file
    single { CacheStorage.Unlimited() }.bind(CacheStorage::class)
    single { HttpClient(LogLevel.ALL, get()) }
    //  singleOf(::RijksmuseumApiImpl).bind(RijksmuseumApi::class)
    singleOf(::SearchRepositoryImpl).bind(SearchRepository::class)
    single<SearchUseCase> { SearchUseCase(get()) }
    viewModelOf(::ArtworksViewModel)
}

private fun HttpClient(
    logLevel: LogLevel,
    cacheStorage: CacheStorage,
    engine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO,
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
                isLenient = true
            }
        )
    }

    install(HttpCache) {
        privateStorage(cacheStorage)
    }

    Logging {
        logger = Logger.SIMPLE
        level = logLevel
    }
}
