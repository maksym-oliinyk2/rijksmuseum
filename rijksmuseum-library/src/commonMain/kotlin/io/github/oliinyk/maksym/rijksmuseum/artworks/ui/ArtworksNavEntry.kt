package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

@Serializable
internal data object ArtworksNavEntry : NavKey

internal fun PolymorphicModuleBuilder<NavKey>.registerArtworksNavEntry() {
    subclass(ArtworksNavEntry::class, ArtworksNavEntry.serializer())
}
