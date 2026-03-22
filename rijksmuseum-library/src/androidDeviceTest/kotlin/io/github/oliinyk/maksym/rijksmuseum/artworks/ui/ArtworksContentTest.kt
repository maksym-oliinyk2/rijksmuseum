package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.DisplayMessageTag
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.ProgressIndicatorTag
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksContent
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksScrollContainerTag
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksViewState
import kotlin.test.Test

..ui

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.DisplayMessageTag
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.ProgressIndicatorTag
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Paginateable
import kotlin.test.Test

// This is actually a multiplatform test, but it's not possible to run it
// reliable because of https://slack-chats.kotlinlang.org/t/18784429/topic
// Once the issue fixed this class can be moved to the commonTest source set
class ArtworksContentTest {

    private companion object {
        const val TestItemsCount = 25
    }

    @Test
    fun artworks_content_displays_all() = runComposeUiTest {
        val artworks = List(TestItemsCount) { i ->
            Artwork(
                url = UrlFrom("https://example.com/$i"),
                title = Title("Artwork $i"),
                primaryImage = UrlFrom("https://example.com/$i.jpg"),
                linguisticObjects = listOf()
            )
        }

        val state = ArtworksViewState(
            artworks = Paginateable.idleList(artworks)
        )

        setContent {
            ArtworksContent(
                state = state,
                onMessage = {}
            )
        }

        artworks.forEach { artwork ->
            onNodeWithTag(ArtworksScrollContainerTag).performScrollToNode(hasTestTag(artwork.title.value))
            onNodeWithTag(artwork.title.value).assertExists()
        }
    }

    @Test
    fun artworks_content_displays_progress_when_loading() = runComposeUiTest {
        val state = ArtworksViewState(
            artworks = Paginateable.loadingList()
        )

        setContent {
            ArtworksContent(
                state = state,
                onMessage = {}
            )
        }

        onNodeWithTag(ProgressIndicatorTag).assertExists()
    }

    @Test
    fun artworks_content_displays_error_when_exception() = runComposeUiTest {
        val errorMessage = "Failed to load artworks"
        val state = ArtworksViewState(
            artworks = Paginateable(
                data = emptyList(),
                state = Paginateable.Exception(AppException(errorMessage))
            )
        )

        setContent {
            ArtworksContent(
                state = state,
                onMessage = {}
            )
        }

        onNodeWithTag(DisplayMessageTag).assertExists()
        onNodeWithText(errorMessage).assertExists()
    }
}
