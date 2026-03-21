package io.github.oliinyk.maksym.rijksmuseum.artwork.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsContent
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsContentTag
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsViewState
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.common.DisplayMessageTag
import io.github.oliinyk.maksym.rijksmuseum.ui.common.ProgressIndicatorTag
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import kotlin.test.Test

// This is actually a multiplatform test, but it's not possible to run it
// reliable because of https://slack-chats.kotlinlang.org/t/18784429/topic
// Once the issue fixed this class can be moved to the commonTest source set
class ArtworkDetailsContentTest {

    private val testUrl = UrlFrom("https://example.com/1")
    private val testArtwork = Artwork(
        url = testUrl,
        title = Title("Night Watch"),
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        descriptions = listOf()
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun when_loading_then_progress_displayed() = runComposeUiTest {
        val state = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable.loadingSingle()
        )

        setContent {
            ArtworkDetailsContent(
                state = state,
                onRefresh = {},
                onReload = {}
            )
        }

        onNodeWithTag(ProgressIndicatorTag).assertIsDisplayed()
        onNodeWithTag(ArtworkDetailsContentTag).assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun when_idle_with_artwork_then_details_displayed() = runComposeUiTest {
        val state = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable.idleSingle(testArtwork)
        )

        setContent {
            ArtworkDetailsContent(
                state = state,
                onRefresh = {},
                onReload = {}
            )
        }

        onNodeWithTag(ArtworkDetailsContentTag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun when_exception_then_error_displayed() = runComposeUiTest {
        val errorMessage = "Failed to load artwork"
        val state = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable(null, Loadable.Exception(AppException(errorMessage)))
        )

        setContent {
            ArtworkDetailsContent(
                state = state,
                onRefresh = {},
                onReload = {}
            )
        }

        onNodeWithTag(DisplayMessageTag)
            .assertIsDisplayed()
            .assertTextEquals(errorMessage)
    }
}
