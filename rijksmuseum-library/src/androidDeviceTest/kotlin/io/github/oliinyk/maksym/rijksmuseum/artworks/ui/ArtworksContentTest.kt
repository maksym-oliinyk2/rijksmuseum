package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.common.DisplayMessageTag
import io.github.oliinyk.maksym.rijksmuseum.ui.common.ProgressIndicatorTag
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paginateable
import kotlin.test.Test

// This is actually a multiplatform test, but it's not possible to run it
// reliable because of https://slack-chats.kotlinlang.org/t/18784429/topic
// Once the issue fixed this class can be moved to the commonTest source set
class ArtworksContentTest {

    private companion object {
        const val TestItemsCount = 25
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun artworksContentDisplaysAllArtworks() = runComposeUiTest {
        val artworks = List(TestItemsCount) { i ->
            Artwork(
                url = UrlFrom("https://example.com/$i"),
                title = Title("Artwork $i"),
                primaryImage = UrlFrom("https://example.com/$i.jpg"),
                descriptions = listOf()
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
            onNode(hasScrollAction()).performScrollToNode(hasTestTag(artwork.title.value))
            onNodeWithTag(artwork.title.value).assertExists()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun artworksContentDisplaysProgressIndicatorWhenLoading() = runComposeUiTest {
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun artworksContentDisplaysErrorMessageWhenException() = runComposeUiTest {
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
