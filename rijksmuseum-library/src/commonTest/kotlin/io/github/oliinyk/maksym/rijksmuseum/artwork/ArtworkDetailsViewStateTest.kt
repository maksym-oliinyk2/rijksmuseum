package io.github.oliinyk.maksym.rijksmuseum.artwork

import arrow.core.left
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArtworkDetailsViewStateTest {

    private val testUrl = UrlFrom("https://example.com/1")
    private val testArtwork = Artwork(
        url = testUrl,
        title = Title("Artwork 1"),
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        descriptions = listOf()
    )

    @Test
    fun `when update with Message OnReload then it returns loading state and load command`() {
        val initialState = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable.idleSingle(testArtwork)
        )
        val (updatedState, commands) = initialState.update(Message.OnReload)

        assertEquals(Loadable.loadingSingle(), updatedState.artwork)
        assertEquals(setOf(LoadCommand(testUrl)), commands)
    }

    @Test
    fun `when update with Message OnRefresh and refreshable then it returns refreshing state and load command`() {
        val initialState = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable.idleSingle(testArtwork)
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(Loadable(testArtwork, Loadable.Refreshing), updatedState.artwork)
        assertEquals(setOf(LoadCommand(testUrl)), commands)
    }

    @Test
    fun `when update with Message OnRefresh and not refreshable then it returns current state and no commands`() {
        val initialState = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable.loadingSingle()
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(initialState, updatedState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when update with Message OnDataLoaded success then it returns idle state with data`() {
        val initialState = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable.loadingSingle()
        )
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(testArtwork.right()))

        assertEquals(Loadable.idleSingle(testArtwork), updatedState.artwork)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when update with Message OnDataLoaded failure then it returns exception state`() {
        val initialState = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable.loadingSingle()
        )
        val exception = AppException("Test Exception")
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(exception.left()))

        assertEquals(Loadable(null, Loadable.Exception(exception)), updatedState.artwork)
        assertTrue(commands.isEmpty())
    }
}
