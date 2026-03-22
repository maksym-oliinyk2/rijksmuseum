package io.github.oliinyk.maksym.rijksmuseum.ui.nav

import androidx.navigation3.runtime.NavKey
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork

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
