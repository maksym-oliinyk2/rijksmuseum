package io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.ArtworkSerializer
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.registerArtworkNavEntry
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.registerArtworksNavEntry
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal class Navigator(
    private val navBackStack: MutableList<NavKey>,
) : List<NavKey> by navBackStack {

    fun navigateBack() {
        navBackStack.removeLastOrNull()
    }

    fun navigateToDetails(artwork: Artwork) {
        navBackStack.add(ArtworkDetailsDestination(artwork))
    }
}

internal val NavSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        contextual(Artwork::class, ArtworkSerializer)
        polymorphic(NavKey::class) {
            registerArtworksNavEntry()
            registerArtworkNavEntry()
        }
    }
}
