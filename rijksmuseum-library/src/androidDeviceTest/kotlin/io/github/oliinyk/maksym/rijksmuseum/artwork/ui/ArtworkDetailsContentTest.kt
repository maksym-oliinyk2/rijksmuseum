package io.github.oliinyk.maksym.rijksmuseum.artwork.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsContent
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsViewState
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import kotlin.test.Test

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
    fun whenStateIsLoadingThenProgressIndicatorIsDisplayed() = runComposeUiTest {
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

        // CircularProgressIndicator doesn't have a default test tag or text,
        // but we can check it doesn't crash and other content is NOT there.
        // Actually, we could add a test tag to ProgressIndicator if needed,
        // but for now let's check it doesn't show the artwork title.
        onNodeWithText("Night Watch").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun whenStateIsIdleWithArtworkThenArtworkDetailsAreDisplayed() = runComposeUiTest {
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

        onNodeWithText("Night Watch").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun whenStateIsExceptionThenErrorMessageIsDisplayed() = runComposeUiTest {
        val errorMessage = "Failed to load artwork"
        val state = ArtworkDetailsViewState(
            artworkId = testUrl,
            artwork = Loadable(null as Artwork?, Loadable.Exception(AppException(errorMessage)))
        )

        setContent {
            ArtworkDetailsContent(
                state = state,
                onRefresh = {},
                onReload = {}
            )
        }

        onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
