package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import arrow.core.left
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksCommand.LoadCommand
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Page
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArtworksViewStateTest {

    private val testArtwork = Artwork(
        url = UrlFrom("https://example.com/1"),
        title = Title("Artwork 1"),
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        descriptions = listOf()
    )

    @Test
    fun `when update with Message OnReload then it returns loading state and load command`() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork))
        )
        val (updatedState, commands) = initialState.update(Message.OnReload)

        assertEquals(Paginateable.loadingList(), updatedState.artworks)
        assertEquals(setOf(LoadCommand(Paging.FirstPage)), commands)
    }

    @Test
    fun `when update with Message OnRefresh and refreshable then it returns refreshing state and load command`() {
        // Refreshable if isIdle and data.isEmpty()
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf())
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(Paginateable(data = listOf(), state = Paginateable.Refreshing), updatedState.artworks)
        assertEquals(setOf(LoadCommand(Paging.FirstPage)), commands)
    }

    @Test
    fun `when update with Message OnRefresh and not refreshable then it returns current state and no commands`() {
        // Not refreshable if has data
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork))
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(initialState, updatedState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when update with Message OnLoadNext and loadable then it returns loading next state and load command`() {
        // Loadable if artworks.hasMore and artworks.isIdle
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork)).copy(hasMore = true)
        )
        val (updatedState, commands) = initialState.update(Message.OnLoadNext)

        assertEquals(
            Paginateable(data = listOf(testArtwork), state = Paginateable.LoadingNext, hasMore = true),
            updatedState.artworks
        )
        assertEquals(setOf(LoadCommand(Paging(1))), commands)
    }

    @Test
    fun `when update with Message OnLoadNext and not loadable then it returns current state and no commands`() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork)).copy(hasMore = false)
        )
        val (updatedState, commands) = initialState.update(Message.OnLoadNext)

        assertEquals(initialState, updatedState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when update with Message OnDataLoaded success then it returns idle state with data`() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.loadingList()
        )
        val newPage = Page(data = listOf(testArtwork), hasMore = true)
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(newPage.right()))

        assertEquals(
            Paginateable(data = listOf(testArtwork), state = Paginateable.Idle, hasMore = true),
            updatedState.artworks
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when update with Message OnDataLoaded success and loading next then it appends data`() {
        val artwork2 = Artwork(
            url = UrlFrom("https://example.com/2"),
            title = Title("Artwork 2"),
            primaryImage = UrlFrom("https://example.com/2.jpg"),
            descriptions = listOf()
        )
        val initialState = ArtworksViewState(
            artworks = Paginateable(
                data = listOf(testArtwork),
                state = Paginateable.LoadingNext,
                hasMore = true
            )
        )
        val nextResult = Page(data = listOf(artwork2), hasMore = false)
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(nextResult.right()))

        assertEquals(
            Paginateable(data = listOf(testArtwork, artwork2), state = Paginateable.Idle, hasMore = false),
            updatedState.artworks
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when update with Message OnDataLoaded failure then it returns exception state`() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.loadingList()
        )
        val exception = RuntimeException("Test Exception")
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(exception.left()))

        assertEquals(Paginateable(data = listOf(), state = Paginateable.Exception(exception)), updatedState.artworks)
        assertTrue(commands.isEmpty())
    }
}
