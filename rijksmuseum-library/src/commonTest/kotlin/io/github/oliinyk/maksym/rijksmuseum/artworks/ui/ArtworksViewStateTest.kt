package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import arrow.core.left
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.ArtworkPreview
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Page
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArtworksViewStateTest {

    private val testArtwork = ArtworkPreview(
        url = UrlFrom("https://example.com/1"),
        title = Title("Artwork 1"),
        images = listOf(UrlFrom("https://example.com/1.jpg"))
    )

    @Test
    fun `test update with Message OnReload`() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork))
        )
        val (updatedState, commands) = initialState.update(Message.OnReload)

        assertEquals(Paginateable.loadingList<ArtworkPreview>(), updatedState.artworks)
        assertEquals(setOf(LoadCommand(Paging.FirstPage)), commands)
    }

    @Test
    fun `test update with Message OnRefresh when refreshable`() {
        // Refreshable if isIdle and data.isEmpty()
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(emptyList())
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(Paginateable(data = emptyList(), state = Paginateable.Refreshing), updatedState.artworks)
        assertEquals(setOf(LoadCommand(Paging.FirstPage)), commands)
    }

    @Test
    fun `test update with Message OnRefresh when not refreshable`() {
        // Not refreshable if has data
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork))
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(initialState, updatedState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `test update with Message OnLoadNext when loadable`() {
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
    fun testUpdateWithMessageOnLoadNextWhenNotLoadable() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork)).copy(hasMore = false)
        )
        val (updatedState, commands) = initialState.update(Message.OnLoadNext)

        assertEquals(initialState, updatedState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `test update with Message OnDataLoaded success`() {
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
    fun `test update with Message OnDataLoaded success appends data when loading next`() {
        val artwork2 = ArtworkPreview(
            url = UrlFrom("https://example.com/2"),
            title = Title("Artwork 2"),
            images = listOf(UrlFrom("https://example.com/2.jpg"))
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
    fun `test update with Message OnDataLoaded failure`() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.loadingList()
        )
        val exception = RuntimeException("Test Exception")
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(exception.left()))

        assertEquals(Paginateable(data = emptyList(), state = Paginateable.Exception(exception)), updatedState.artworks)
        assertTrue(commands.isEmpty())
    }
}
