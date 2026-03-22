package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation

import arrow.core.left
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.NonEmptyString
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Page
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Paging
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksCommand.LoadCommand
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.exception_unknown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArtworksViewStateTest {

    private val testArtwork = Artwork(
        url = UrlFrom("https://example.com/1"),
        title = NonEmptyString("Artwork 1"),
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        linguisticObjects = listOf()
    )

    @Test
    fun when_OnReload_then_loading() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork))
        )
        val (updatedState, commands) = initialState.update(Message.OnReload)

        assertEquals(Paginateable.loadingList(), updatedState.artworks)
        assertEquals(setOf(LoadCommand(Paging.FirstPage)), commands)
    }

    @Test
    fun when_OnRefresh_refreshable_then_refreshing() {
        // Refreshable if isIdle and data.isEmpty()
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf())
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(Paginateable(data = listOf(), state = Paginateable.Refreshing), updatedState.artworks)
        assertEquals(setOf(LoadCommand(Paging.FirstPage)), commands)
    }

    @Test
    fun when_OnRefresh_not_refreshable_then_no_change() {
        // Not refreshable if has data
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork))
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(initialState, updatedState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun when_OnLoadNext_loadable_then_loading_next() {
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
    fun when_OnLoadNext_not_loadable_then_no_change() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.idleList(listOf(testArtwork)).copy(hasMore = false)
        )
        val (updatedState, commands) = initialState.update(Message.OnLoadNext)

        assertEquals(initialState, updatedState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun when_OnDataLoaded_success_then_idle_with_data() {
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
    fun when_OnDataLoaded_success_loading_next_then_appends_data() {
        val artwork2 = Artwork(
            url = UrlFrom("https://example.com/2"),
            title = NonEmptyString("Artwork 2"),
            primaryImage = UrlFrom("https://example.com/2.jpg"),
            linguisticObjects = listOf()
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
    fun when_OnDataLoaded_failure_then_exception() {
        val initialState = ArtworksViewState(
            artworks = Paginateable.loadingList()
        )
        val exception = AppException(Res.string.exception_unknown)
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(exception.left()))

        assertEquals(Paginateable(data = listOf(), state = Paginateable.Exception(exception)), updatedState.artworks)
        assertTrue(commands.isEmpty())
    }
}
