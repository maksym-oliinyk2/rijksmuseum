package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ValueHolder
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork

internal class Navigator(
    private val navBackStack: NavBackStack<NavKey>,
    private val artworkValueHolder: ValueHolder<Artwork>,
) : List<NavKey> by navBackStack {

    fun navigateBack() {
        navBackStack.removeLastOrNull()
    }

    fun navigateToDetails(artwork: Artwork) {
        artworkValueHolder.value = artwork
        navBackStack.add(ArtworkDetailsDestination(artwork.url))
    }
}
