package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.NonEmptyString
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.DisplayMessageTag
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.ProgressIndicatorTag
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Paginateable
import kotlin.test.Test

class ArtworksContentTest {

    private companion object {
        const val TestItemsCount = 25
    }

    @Test
    fun artworks_content_displays_all() = runComposeUiTest {
        val artworks = List(TestItemsCount) { i ->
            Artwork(
                url = UrlFrom("https://example.com/$i"),
                title = NonEmptyString("Artwork $i"),
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
