package io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav

import androidx.navigation3.runtime.NavKey
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsDestination

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
