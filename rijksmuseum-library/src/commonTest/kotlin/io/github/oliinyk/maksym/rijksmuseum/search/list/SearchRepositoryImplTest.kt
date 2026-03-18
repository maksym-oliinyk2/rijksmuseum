package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SearchRepositoryImplTest {

    private class FakeRijksmuseumApi(
        private val artworksDetails: Map<String, Artwork>,
        private val searchResponses: Map<Url, SearchResponse> = emptyMap(),
        private val defaultSearchResponse: SearchResponse? = null,
    ) : RijksmuseumApi {
        override suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse> =
            (searchResponses[url] ?: defaultSearchResponse
            ?: throw IllegalArgumentException("No search response for $url")).right()

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

        val api = FakeRijksmuseumApi(
            artworksDetails = artworksDetails,
            defaultSearchResponse = searchResponse
        )
        val repository = SearchRepositoryImpl(api)

        // Execute
        val paging = Paging(currentSize = 0, resultsPerPage = 2)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertNotNull(page)
        assertEquals(2, page.data.size)
        assertEquals(artwork1, page.data[0])
        assertEquals(artwork2, page.data[1])
        assertTrue(page.hasMore)
    }

    @Test
    fun `test fetchArtworks when cache hit with enough items`() = runTest {
        // Setup
        val item1Id = "item-1"
        val item2Id = "item-2"
        val item3Id = "item-3"

        val initialSearchResponse = SearchResponse(
            next = null,
            orderedItems = listOf(
                Item(id = item1Id),
                Item(id = item2Id),
                Item(id = item3Id)
            )
        )

        val artworksDetails = initialSearchResponse.orderedItems.associate { item ->
            item.id to Artwork(
                url = UrlFrom(item.id),
                title = Title("Title for ${item.id}"),
                images = listOf(UrlFrom("https://image.url/${item.id}"))
            )
        }

        val api = FakeRijksmuseumApi(artworksDetails = artworksDetails)
        val cache = InMemoryCache(initialSearchResponse)
        val repository = SearchRepositoryImpl(api, cache)

        // Execute: Ask for 2 items starting from index 0
        val paging = Paging(currentSize = 0, resultsPerPage = 2)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertNotNull(page)
        assertEquals(2, page.data.size)
        assertEquals("Title for $item1Id", page.data[0].title.value)
        assertEquals("Title for $item2Id", page.data[1].title.value)
        assertTrue(page.hasMore) // 3 items total, 0+2 < 3
    }

    @Test
    fun `test fetchArtworks when cache hit and it's the end of pagination`() = runTest {
        // Setup
        val item1Id = "item-1"

        val initialSearchResponse = SearchResponse(
            next = null,
            orderedItems = listOf(Item(id = item1Id))
        )

        val api = FakeRijksmuseumApi(artworksDetails = emptyMap())
        val cache = InMemoryCache(initialSearchResponse)
        val repository = SearchRepositoryImpl(api, cache)

        // Execute: currentSize = 1, resultsPerPage = 1. items.size = 1.
        // The condition currentCachedResponse.orderedItems.size >= paging.currentSize + paging.resultsPerPage
        // becomes 1 >= 1 + 1, which is false.
        // Then currentCachedResponse.next == null is true.
        val paging = Paging(currentSize = 1, resultsPerPage = 1)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertEquals(Page.End as Page<Artwork>, page)
    }

    @Test
    fun `test fetchArtworks when cache hit but not enough items needs to fetch next page`() =
        runTest {
            // Setup
            val item1Id = "item-1"
            val item2Id = "item-2"
            val item3Id = "item-3"

            val initialSearchResponse = SearchResponse(
                next = OrderedCollectionPage(id = "next-page-url"),
                orderedItems = listOf(Item(id = item1Id))
            )

            val nextSearchResponse = SearchResponse(
                next = null,
                orderedItems = listOf(Item(id = item2Id), Item(id = item3Id))
            )

            val artworksDetails = listOf(item1Id, item2Id, item3Id).associateWith { id ->
                Artwork(
                    url = UrlFrom(id),
                    title = Title("Title for $id"),
                    images = listOf(UrlFrom("https://image.url/$id"))
                )
            }.mapKeys { it.key } // The map keys are already correct strings

            val api = FakeRijksmuseumApi(
                artworksDetails = artworksDetails,
                searchResponses = mapOf(UrlFrom("next-page-url") to nextSearchResponse)
            )
            val cache = InMemoryCache(initialSearchResponse)
            val repository = SearchRepositoryImpl(api, cache)

            // Execute: currentSize = 0, resultsPerPage = 2.
            // cache has only 1 item. Need to fetch next page.
            // fromPreviousPage will be [item-1]
            // newPage will be nextSearchResponse
            // currAsync will take (resultsPerPage - fromPreviousPage.size) = (2 - 1) = 1 item from newPage
            val paging = Paging(currentSize = 0, resultsPerPage = 2)
            val page = repository.fetchArtworks(paging).getOrNull()

            // Verify
            assertNotNull(page)
            assertEquals(2, page.data.size)
            assertEquals("Title for $item1Id", page.data[0].title.value)
            assertEquals("Title for $item2Id", page.data[1].title.value)
            assertTrue(page.hasMore) // nextSearchResponse has 2 items, we took 1. 2 - 1 > 0.
    }
}
