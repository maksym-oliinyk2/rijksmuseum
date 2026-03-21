package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsContentTag
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ValueHolder
import io.github.oliinyk.maksym.rijksmuseum.artwork.detailsModule
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepository
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksScrollContainerTag
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksViewModel
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.Navigator
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.module
import kotlin.test.Test

private class TestRijksmuseumApi(
    private val artworksDetails: Map<Url, Either<io.github.oliinyk.maksym.rijksmuseum.artworks.AppException, Artwork>>,
    private val searchResponses:
    Map<Url, Either<io.github.oliinyk.maksym.rijksmuseum.artworks.AppException, PaginatedIds>> = emptyMap(),
) : RijksmuseumApi {
    override suspend fun fetchArtworkIds(
        page: Url
    ): Either<io.github.oliinyk.maksym.rijksmuseum.artworks.AppException, PaginatedIds> =
        searchResponses[page] ?: error("No search response for $page")

    override suspend fun fetchArtwork(
        url: Url
    ): Either<io.github.oliinyk.maksym.rijksmuseum.artworks.AppException, Artwork> =
        artworksDetails[url] ?: error("No artwork for $url")
}

class AppTest {

    private companion object {
        const val ArtworksCount = 20
        const val TenthIndex = 9
    }

    @OptIn(ExperimentalTestApi::class, org.koin.core.annotation.KoinExperimentalAPI::class)
    @Test
    fun when_composing_items_then_correct_sequence_of_values_is_produced() = runComposeUiTest {
        val artworks = GenerateArtworks(ArtworksCount)
        val testApi = TestRijksmuseumApi(
            artworksDetails = artworks.associate { it.url to it.right() },
            searchResponses = mapOf(
                RijksmuseumApi.InitialPageUrl to PaginatedIds(
                    next = null,
                    ids = artworks.map { it.url }
                ).right()
            )
        )

        val testModule = module {
            viewModelOf(::ArtworksViewModel)
            single<RijksmuseumApi> { testApi }
            single<SearchRepository> { SearchRepositoryImpl(get()) }
            single { SearchUseCase(get()) }
        }

        setContent {
            App { backStack ->
                KoinConfiguration {
                    modules(
                        testModule,
                        detailsModule,
                        module {
                            single { backStack }
                            single { Navigator(get(), get(named<Artwork>())) }
                            single(named<Artwork>()) { ValueHolder<Artwork>() }
                        }
                    )
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

private fun GenerateArtworks(
    size: Int,
): List<Artwork> = List(size) { i ->
    Artwork(
        url = UrlFrom("https://example.com/$i"),
        title = Title("Artwork $i"),
        primaryImage = UrlFrom("https://example.com/$i.jpg"),
        descriptions = listOf()
    )
}