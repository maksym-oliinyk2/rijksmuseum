package io.github.oliinyk.maksym.rijksmuseum.di

import io.github.oliinyk.maksym.rijksmuseum.artworks.details.detailsModule
import io.github.oliinyk.maksym.rijksmuseum.search.list.SearchModule
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import org.koin.dsl.module

@Suppress("FunctionName")
internal fun AppModule(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
) = module {
    includes(SearchModule(engine), detailsModule)
}
