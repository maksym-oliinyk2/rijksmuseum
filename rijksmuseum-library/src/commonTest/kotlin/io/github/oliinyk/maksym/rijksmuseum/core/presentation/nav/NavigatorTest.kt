package io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav

import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigatorTest {

    private val testArtwork = Artwork(
        url = UrlFrom("https://example.com/1"),
        title = "Artwork 1",
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        linguisticObjects = listOf()
    )

    @Test
    fun when_navigateToDetails_then_destination_added_to_back_stack() {
        val backStack = mutableListOf<androidx.navigation3.runtime.NavKey>(ArtworksDestination)
        val navigator = Navigator(backStack)

        navigator.navigateToDetails(testArtwork)

        assertEquals(2, navigator.size)
        assertEquals(ArtworkDetailsDestination(testArtwork), navigator.last())
    }

    @Test
    fun when_navigateBack_then_last_entry_removed() {
        val backStack = mutableListOf(
            ArtworksDestination,
            ArtworkDetailsDestination(testArtwork)
        )
        val navigator = Navigator(backStack)

        navigator.navigateBack()

        assertEquals(1, navigator.size)
        assertEquals(ArtworksDestination, navigator.last())
    }

    @Test
    fun when_navigateBack_on_empty_stack_then_no_exception() {
        val navigator = Navigator(mutableListOf())

        navigator.navigateBack()

        assertTrue(navigator.isEmpty())
    }

    @Test
    fun when_navigateToDetails_multiple_times_then_all_destinations_added() {
        val backStack = mutableListOf<androidx.navigation3.runtime.NavKey>(ArtworksDestination)
        val navigator = Navigator(backStack)
        val artwork2 = testArtwork.copy(url = UrlFrom("https://example.com/2"), title = "Artwork 2")

        navigator.navigateToDetails(testArtwork)
        navigator.navigateToDetails(artwork2)

        assertEquals(3, navigator.size)
        assertEquals(ArtworkDetailsDestination(artwork2), navigator.last())
    }

    @Test
    fun when_navigateBack_after_navigateToDetails_then_returns_to_previous_screen() {
        val backStack = mutableListOf<androidx.navigation3.runtime.NavKey>(ArtworksDestination)
        val navigator = Navigator(backStack)

        navigator.navigateToDetails(testArtwork)
        navigator.navigateBack()

        assertEquals(1, navigator.size)
        assertEquals(ArtworksDestination, navigator.first())
    }
}
