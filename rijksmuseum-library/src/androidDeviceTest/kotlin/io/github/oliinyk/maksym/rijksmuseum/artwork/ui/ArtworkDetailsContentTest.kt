package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Loadable
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.DisplayMessageTag
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsContent
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsContentTag
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsRefreshIndicatorTag
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsViewState
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
        linguisticObjects = listOf()
    )

    @Test
    fun when_idle_with_artwork_then_details_displayed() = runComposeUiTest {
        val state = ArtworkDetailsViewState(
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

    @Test
    fun when_exception_then_error_displayed() = runComposeUiTest {
        val errorMessage = "Failed to load artwork"
        val state = ArtworkDetailsViewState(
            artwork = Loadable(testArtwork, Loadable.Exception(AppException(errorMessage)))
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

    @Test
    fun when_refreshing_then_progress_displayed() = runComposeUiTest {
        val state = ArtworkDetailsViewState(
            artwork = Loadable(testArtwork, Loadable.Refreshing)
        )

        setContent {
            ArtworkDetailsContent(
                state = state,
                onRefresh = {},
                onReload = {}
            )
        }

        onNodeWithTag(ArtworkDetailsRefreshIndicatorTag).assertIsDisplayed()
    }
}
