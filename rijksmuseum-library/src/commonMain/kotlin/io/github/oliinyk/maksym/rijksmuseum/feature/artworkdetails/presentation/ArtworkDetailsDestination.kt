package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import androidx.navigation3.runtime.NavKey
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.data.ArtworkSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

// artwork is not expected to be huge for serialization, otherwise we can use in memory cache to pass
// instance between screens
@Serializable
internal data class ArtworkDetailsDestination(
    @Serializable(with = ArtworkSerializer::class)
    val artwork: Artwork
) : NavKey

internal fun PolymorphicModuleBuilder<NavKey>.registerArtworkNavEntry() {
    subclass(ArtworkDetailsDestination::class, ArtworkDetailsDestination.serializer())
}
