package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.navigation3.runtime.NavBackStack
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ValueHolder
import io.github.oliinyk.maksym.rijksmuseum.artwork.detailsModule
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.searchModule
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksDestination
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.Navigator
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
import org.koin.core.qualifier.named
import org.koin.dsl.module

@Suppress("FunctionName")
internal fun AppModule(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
) = module {
    includes(searchModule, detailsModule)
    single { HttpClient(LogLevel.ALL, engine) }
    single { Navigator(NavBackStack(ArtworksDestination), get(named<Artwork>())) }
    single(named<Artwork>()) { ValueHolder<Artwork>() }
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
