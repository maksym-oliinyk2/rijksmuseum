package io.github.oliinyk.maksym.rijksmuseum.app

import io.github.oliinyk.maksym.rijksmuseum.artwork.detailsModule
import io.github.oliinyk.maksym.rijksmuseum.artworks.SearchModule
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import org.koin.dsl.module

@Suppress("FunctionName")
internal fun AppModule(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
) = module {
    includes(SearchModule(engine), detailsModule)
}
