package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchRepositoryImplTest {

    private class FakeRijksmuseumApi(
        private val searchResponse: SearchResponse,
        private val artworksDetails: Map<String, Artwork>
    ) : RijksmuseumApi {
        override suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse> =
            searchResponse.right()

        override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
            artworksDetails[url.toExternalValue()]?.right()
                ?: throw IllegalArgumentException("No artwork for $url")
    }

    @Test
    fun `test fetchArtworks when cache is null`() = runTest {
        // Setup
        val item1Id = "en-SK-A-3262"
        val item2Id = "en-SK-A-4878"

        val searchResponse = SearchResponse(
            next = OrderedCollectionPage(id = "next-page-id"),
            orderedItems = listOf(
                Item(id = item1Id),
                Item(id = item2Id),
                Item(id = "item-3"),
                Item(id = "item-4"),
                Item(id = "item-5"),
                Item(id = "item-6"),
                Item(id = "item-7"),
                Item(id = "item-8"),
                Item(id = "item-9"),
                Item(id = "item-10"),
                Item(id = "item-11")
            )
        )

        val artwork1 = Artwork(
            url = UrlFrom(item1Id),
            title = Title("The Night Watch"),
            images = listOf(UrlFrom("https://lh3.googleusercontent.com/NF7Z_E-S_6e-M-p8Bf8Bf8B"))
        )
        val artwork2 = Artwork(
            url = UrlFrom(item2Id),
            title = Title("The Milkmaid"),
            images = listOf(UrlFrom("https://lh3.googleusercontent.com/c6_9-f1_y-p8Bf8Bf8Bf8B"))
        )

        // Fill the details for all items to avoid errors during parMap
        val artworksDetails = searchResponse.orderedItems.associate { item ->
            item.id to Artwork(
                url = UrlFrom(item.id),
                title = Title("Title for ${item.id}"),
                images = listOf(UrlFrom("https://image.url/${item.id}"))
            )
        }.toMutableMap()

        artworksDetails[item1Id] = artwork1
        artworksDetails[item2Id] = artwork2

        val api = FakeRijksmuseumApi(searchResponse, artworksDetails)
        val repository = SearchRepositoryImpl(api)

        // Execute
        val paging = Paging(currentSize = 0, resultsPerPage = 2)
        val result = repository.fetchArtworks(paging)

        // Verify
        assertTrue(result.isRight(), "Result should be Right but was $result")
        val page = result.getOrNull()!!
        assertEquals(2, page.data.size)
        assertEquals(artwork1, page.data[0])
        assertEquals(artwork2, page.data[1])
        assertTrue(page.hasMore)
    }
}
