package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.ArtworkPreview
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
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
    fun testArtworksContentDisplaysAllArtworks() = runComposeUiTest {
        val artworks = List(TestItemsCount) { i ->
            ArtworkPreview(
                url = UrlFrom("https://example.com/$i"),
                title = Title("Artwork $i"),
                images = listOf(UrlFrom("https://example.com/$i.jpg"))
            )
        }

        val state = ArtworksViewState(
            artworks = Paginateable.idleList(artworks)
        )

        setContent {
            ArtworksContent(
                state = state,
                onRefresh = {},
                onReload = {},
                onLoadNext = {},
                onNavigateToDetails = {}
            )
        }

        artworks.forEach { artwork ->
            onNode(hasScrollAction()).performScrollToNode(hasTestTag(artwork.title.value))
            onNodeWithTag(artwork.title.value).assertExists()
        }
    }
}
