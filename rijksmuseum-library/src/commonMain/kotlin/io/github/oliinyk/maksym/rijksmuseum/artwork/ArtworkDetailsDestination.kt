package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.navigation3.runtime.NavKey
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.ArtworkSerializer
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
