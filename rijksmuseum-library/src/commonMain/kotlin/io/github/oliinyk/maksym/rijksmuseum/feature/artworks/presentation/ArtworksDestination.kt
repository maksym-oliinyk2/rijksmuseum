package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

@Serializable
internal data object ArtworksDestination : NavKey

internal fun PolymorphicModuleBuilder<NavKey>.registerArtworksNavEntry() {
    subclass(ArtworksDestination::class, ArtworksDestination.serializer())
}
