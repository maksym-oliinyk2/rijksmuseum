package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.navigation3.runtime.NavBackStack
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ValueHolder
import io.github.oliinyk.maksym.rijksmuseum.artwork.detailsModule
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.SearchModule
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksDestination
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module

@Suppress("FunctionName")
internal fun AppModule(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
) = module {
    includes(SearchModule(engine), detailsModule)
    single { NavBackStack(ArtworksDestination) }
    single(named<Artwork>()) { ValueHolder<Artwork>() }
}
