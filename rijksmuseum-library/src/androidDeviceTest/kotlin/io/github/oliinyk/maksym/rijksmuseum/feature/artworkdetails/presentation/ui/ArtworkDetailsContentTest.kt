package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.NonEmptyString
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Loadable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.toException
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsContent
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsContentTag
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsExceptionIndicatorTag
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
        title = NonEmptyString("Night Watch"),
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        linguisticObjects = listOf()
    )

    @Test
    fun when_idle_with_artwork_then_details_displayed() = runComposeUiTest {
        val state = ArtworkDetailsViewState(
            loadable = Loadable.idleSingle(testArtwork)
        )

        setContent {
            ArtworkDetailsContent(
                state = state,
                onRefresh = {},
                onBack = {},
            )
        }

        onNodeWithTag(ArtworkDetailsContentTag).assertIsDisplayed()
    }

    @Test
    fun when_refreshing_then_progress_displayed() = runComposeUiTest {
        val state = ArtworkDetailsViewState(
            loadable = Loadable(testArtwork, Loadable.Refreshing)
        )

        setContent {
            ArtworkDetailsContent(
                state = state,
                onRefresh = {},
                onBack = {},
            )
        }

        onNodeWithTag(ArtworkDetailsRefreshIndicatorTag).assertIsDisplayed()
    }

    @Test
    fun when_refresh_triggers_exception_then_exception_indicator_displayed() = runComposeUiTest {
        val exception = AppException("Network error")
        val state = ArtworkDetailsViewState(
            loadable = Loadable.idleSingle(testArtwork).toException(exception)
        )

        setContent {
            ArtworkDetailsContent(
                state = state,
                onRefresh = {},
                onBack = {},
            )
        }

        onNodeWithTag(ArtworkDetailsExceptionIndicatorTag).assertIsDisplayed()
    }
}
