package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import arrow.core.left
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.NonEmptyString
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Loadable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArtworkDetailsViewStateTest {

    private val testUrl = UrlFrom("https://example.com/1")
    private val testArtwork = Artwork(
        url = testUrl,
        title = NonEmptyString("Artwork 1"),
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        linguisticObjects = listOf()
    )

    @Test
    fun when_OnReload_then_loading() {
        val initialState = ArtworkDetailsViewState(
            loadable = Loadable.idleSingle(testArtwork)
        )
        val (updatedState, commands) = initialState.update(Message.OnReload)

        assertEquals(Loadable(testArtwork, Loadable.Loading), updatedState.loadable)
        assertEquals(setOf(LoadCommand(testUrl)), commands)
    }

    @Test
    fun when_OnRefresh_refreshable_then_refreshing() {
        val initialState = ArtworkDetailsViewState(
            loadable = Loadable.idleSingle(testArtwork)
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(Loadable(testArtwork, Loadable.Refreshing), updatedState.loadable)
        assertEquals(setOf(LoadCommand(testUrl)), commands)
    }

    @Test
    fun when_OnRefresh_not_refreshable_then_no_change() {
        val initialState = ArtworkDetailsViewState(
            loadable = Loadable(testArtwork, Loadable.Loading)
        )
        val (updatedState, commands) = initialState.update(Message.OnRefresh)

        assertEquals(initialState, updatedState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun when_OnDataLoaded_success_then_idle_with_data() {
        val initialState = ArtworkDetailsViewState(
            loadable = Loadable(testArtwork, Loadable.Loading)
        )
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(testArtwork.right()))

        assertEquals(Loadable.idleSingle(testArtwork), updatedState.loadable)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun when_OnDataLoaded_failure_then_exception() {
        val initialState = ArtworkDetailsViewState(
            loadable = Loadable(testArtwork, Loadable.Loading)
        )
        val exception = AppException("Test Exception")
        val (updatedState, commands) = initialState.update(Message.OnDataLoaded(exception.left()))

        assertEquals(Loadable(testArtwork, Loadable.Exception(exception)), updatedState.loadable)
        assertTrue(commands.isEmpty())
    }
}
