package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.BuildConfig.InitialPageUrl
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsContentTag
import io.github.oliinyk.maksym.rijksmuseum.artwork.DetailsModule
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.SearchModule
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.list.TestRijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksScrollContainerTag
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.nav.Navigator
import io.github.xlopec.tea.core.ShareOptions
import kotlinx.coroutines.flow.SharingStarted
import org.koin.core.module.Module
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import kotlin.test.Test

class AppTest {

    private companion object {
        const val ArtworksCount = 20
        const val TenthIndex = 9
    }

    @Test
    fun when_artwork_clicked_then_details_displayed() = runComposeUiTest {
        val artworks = List(ArtworksCount) { i ->
            Artwork(
                url = UrlFrom("https://example.com/$i"),
                title = Title("Artwork $i"),
                primaryImage = UrlFrom("https://example.com/$i.jpg"),
                linguisticObjects = listOf()
            )
        }
        val testApi = TestRijksmuseumApi(
            artworksDetails = artworks.associate { it.url to it.right() },
            searchResponses = mapOf(
                InitialPageUrl to PaginatedIds(
                    next = null,
                    ids = artworks.map { it.url }
                ).right()
            )
        )

        setContent {
            App { backStack ->
                KoinConfiguration {
                    modules(TestAppModule(testApi, backStack), SearchModule, DetailsModule)
                }
            }
        }

        val expectedArtwork = artworks[TenthIndex]
        // User scrolls to 10-th item from the list of artworks
        onNodeWithTag(ArtworksScrollContainerTag)
            .performScrollToNode(hasTestTag(expectedArtwork.title.value))
        onNodeWithTag(expectedArtwork.title.value).assertExists()
        // User taps on the 10-th item in the list
        onNodeWithTag(expectedArtwork.title.value).performClick()
        // User navigates to the artwork details screen
        onNodeWithTag(ArtworkDetailsContentTag).assertExists()
        // Artwork details screen is displayed
        onNodeWithText(expectedArtwork.title.value).assertExists()
    }
}

private fun TestAppModule(
    testApi: TestRijksmuseumApi,
    backStack: NavBackStack<NavKey>
): Module = module {
    single { ShareOptions(SharingStarted.Lazily, 1u) }
    single { testApi }.bind(RijksmuseumApi::class)
    single { Navigator(backStack) }
}
