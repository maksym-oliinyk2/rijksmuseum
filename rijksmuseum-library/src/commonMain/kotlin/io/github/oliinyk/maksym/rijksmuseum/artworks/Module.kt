package io.github.oliinyk.maksym.rijksmuseum.artworks

import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApiImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepository
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind

@Suppress("FunctionName")
internal fun SearchModule(engine: HttpClientEngineFactory<HttpClientEngineConfig>) = module {
    single { HttpClient(LogLevel.ALL, engine) }
    singleOf(::SearchApiImpl).bind(SearchApi::class)
    single { SearchRepositoryImpl(get()) }.bind(SearchRepository::class)
    singleOf(::SearchUseCase)
    viewModel { ArtworksViewModel(get()) }
}

private fun HttpClient(
    logLevel: LogLevel,
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
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

    Logging {
        logger = Logger.SIMPLE
        level = logLevel
    }
}
