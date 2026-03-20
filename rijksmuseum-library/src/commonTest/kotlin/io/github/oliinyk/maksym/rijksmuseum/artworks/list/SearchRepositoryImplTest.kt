package io.github.oliinyk.maksym.rijksmuseum.artworks.list

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchUrl
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Page
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SearchRepositoryImplTest {

    @Test
    fun `test fetchArtworks when cache is null`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/en-SK-A-3262")
        val item2Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/en-SK-A-4878")

        val searchResponse = createSearchResponse(
            item1Id,
            item2Id,
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-3"),
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-4"),
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-5"),
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-6"),
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-7"),
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-8"),
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-9"),
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-10"),
            UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-11")
        )

        val artwork1 = Artwork(
            url = item1Id,
            title = Title("The Night Watch"),
            images = listOf(UrlFrom("https://lh3.googleusercontent.com/NF7Z_E-S_6e-M-p8Bf8Bf8B")),
            descriptions = emptyList(),
        )
        val artwork2 = Artwork(
            url = item2Id,
            title = Title("The Milkmaid"),
            images = listOf(UrlFrom("https://lh3.googleusercontent.com/c6_9-f1_y-p8Bf8Bf8Bf8B")),
            descriptions = emptyList(),
        )

        // Fill the details for all items to avoid errors during parMap
        val artworksDetails = searchResponse.items.associate { item ->
            item.id to Artwork(
                url = item.id,
                title = Title("Title for ${item.id}"),
                images = listOf(UrlFrom("https://image.url/${item.id}")),
                descriptions = emptyList(),
            )
        }.toMutableMap()

        artworksDetails[item1Id] = artwork1
        artworksDetails[item2Id] = artwork2

        val api = TestSearchApi(
            artworksDetails = artworksDetails,
            searchResponses = mapOf(SearchUrl to searchResponse)
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
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
        val item2Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-2")
        val item3Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-3")

        val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(
                HumanMadeObjectResponse.ArtworkIdItem(id = item1Id),
                HumanMadeObjectResponse.ArtworkIdItem(id = item2Id),
                HumanMadeObjectResponse.ArtworkIdItem(id = item3Id)
            )
        )

        val artworksDetails = initialSearchResponse.items.associate { item ->
            item.id to Artwork(
                url = item.id,
                title = Title("Title for ${item.id}"),
                images = listOf(UrlFrom("https://image.url/${item.id}")),
                descriptions = emptyList(),
            )
        }

        val api = TestSearchApi(
            artworksDetails = artworksDetails,
            searchResponses = mapOf(SearchUrl to initialSearchResponse)
        )
        val repository = SearchRepositoryImpl(api)

        // Execute: Ask for 2 items starting from index 0
        val paging = Paging(currentSize = 0, resultsPerPage = 2)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertNotNull(page)
        assertEquals(artworksDetails.values.take(paging.resultsPerPage), page.data)
        assertTrue(page.hasMore) // 3 items total, 0+2 < 3
    }

    @Test
    fun `test fetchArtworks when cache hit and it's the end of pagination`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")

        val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
        )

        val api = TestSearchApi(
            artworksDetails = emptyMap<Url, Artwork>(),
            searchResponses = mapOf(SearchUrl to initialSearchResponse)
        )
        val repository = SearchRepositoryImpl(
            api = api,
            cachedIds = listOf(item1Id),
            nextUrl = null
        )

        // Execute: currentSize = 1, resultsPerPage = 1. items.size = 1.
        val paging = Paging(currentSize = 1, resultsPerPage = 1)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertSame(Page.End, page as Page<*>)
    }

    @Test
    fun `test fetchArtworks when cache hit but not enough items needs to fetch next page`() =
        runTest {
            // Setup
            val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
            val item2Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-2")
            val item3Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-3")

            val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
                next = HumanMadeObjectResponse.NextPage(
                    id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2")
                ),
                items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
            )

            val nextSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
                next = null,
                items = listOf(
                    HumanMadeObjectResponse.ArtworkIdItem(id = item2Id),
                    HumanMadeObjectResponse.ArtworkIdItem(id = item3Id)
                )
            )

            val artworksDetails = listOf(item1Id, item2Id, item3Id).associateWith { id ->
                Artwork(
                    url = id,
                    title = Title("Title for $id"),
                    images = listOf(UrlFrom("https://image.url/${id.toExternalValue().substringAfterLast("/")}")),
                    descriptions = emptyList()
                )
            }

            val api = TestSearchApi(
                artworksDetails = artworksDetails,
                searchResponses = mapOf(
                    SearchUrl to initialSearchResponse,
                    UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2") to nextSearchResponse
                )
            )
            val repository = SearchRepositoryImpl(api)

            // Execute: currentSize = 0, resultsPerPage = 2.
            // cache has only 1 item. Need to fetch next page.
            // fromPreviousPage will be [item-1]
            // newPage will be nextSearchResponse
            // currAsync will take (resultsPerPage - fromPreviousPage.size) = (2 - 1) = 1 item from newPage
            val paging = Paging(currentSize = 0, resultsPerPage = 2)
            val page = repository.fetchArtworks(paging).getOrNull()

            // Verify
            assertNotNull(page)
            assertEquals(artworksDetails.values.take(paging.resultsPerPage), page.data)
            assertTrue(page.hasMore) // nextSearchResponse has 2 items, we took 1. 2 - 1 > 0.
        }

    @Test
    fun `test fetchArtworks when request is beyond cache and no more pages`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
        val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
        )
        val api = TestSearchApi(
            artworksDetails = emptyMap<Url, Artwork>(),
            searchResponses = mapOf(SearchUrl to initialSearchResponse)
        )
        val repository = SearchRepositoryImpl(
            api = api,
            cachedIds = listOf(item1Id),
            nextUrl = null
        )

        // Execute: Ask for items at index 5, but only 1 exists
        val paging = Paging(currentSize = 5, resultsPerPage = 2)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertSame(Page.End, page as Page<*>)
    }

    @Test
    fun `test fetchArtworks when multiple pages need to be fetched`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
        val item2Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-2")
        val item3Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-3")

        val response1 = HumanMadeObjectResponse.ArtworksResponse(
            next = HumanMadeObjectResponse.NextPage(
                id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2")
            ),
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
        )
        val response2 = HumanMadeObjectResponse.ArtworksResponse(
            next = HumanMadeObjectResponse.NextPage(
                id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=3")
            ),
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item2Id))
        )
        val response3 = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item3Id))
        )

        val artworksDetails = listOf(item1Id, item2Id, item3Id).associateWith { id ->
            Artwork(
                url = id,
                title = Title("Title for $id"),
                images = listOf(UrlFrom("https://image.url/${id.toExternalValue().substringAfterLast("/")}")),
                descriptions = emptyList()
            )
        }

        val api = TestSearchApi(
            artworksDetails = artworksDetails,
            searchResponses = mapOf(
                SearchUrl to response1,
                UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2") to response2,
                UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=3") to response3
            )
        )
        val repository = SearchRepositoryImpl(api)

        // Execute: resultsPerPage = 3. Cache only has 1 initially. Need to fetch page-2 and page-3.
        val paging = Paging(currentSize = 0, resultsPerPage = 3)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertNotNull(page)
        assertEquals(3, page.data.size)
        assertEquals(artworksDetails.values.toList(), page.data)
    }

    @Test
    fun `test fetchArtworks when fetching details fails`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
        val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
        )

        val exception = AppException("Network error")
        val repository = SearchRepositoryImpl(FailingSearchApi(initialSearchResponse, exception))

        // Execute
        val paging = Paging(currentSize = 0, resultsPerPage = 1)
        val result = repository.fetchArtworks(paging)

        // Verify
        val actualException = result.leftOrNull()
        assertNotNull(actualException)
        assertEquals(exception, actualException)
    }

    @Test
    fun `test hasMore when exactly at the end of items and no next page`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
        val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
        )
        val artworksDetails = mapOf(
            item1Id to Artwork(
                url = item1Id,
                title = Title("Title"),
                images = emptyList(),
                descriptions = emptyList()
            )
        )
        val api = TestSearchApi(
            artworksDetails = artworksDetails,
            searchResponses = mapOf(SearchUrl to initialSearchResponse)
        )
        val repository = SearchRepositoryImpl(
            api = api,
            cachedIds = listOf(item1Id),
            nextUrl = null
        )

        // Execute: currentSize = 0, resultsPerPage = 1. items.size = 1.
        // limit = 0 + 1 = 1.
        // cachedIds.size (1) > limit (1) is false. nextUrl is null.
        val hasMore = repository.fetchArtworks(Paging(currentSize = 0, resultsPerPage = 1))
            .getOrNull()?.hasMore

        // Verify
        assertEquals(false, hasMore)
    }
}

private class FailingSearchApi(
    private val searchResponse: HumanMadeObjectResponse.ArtworksResponse,
    private val exception: AppException,
) : SearchApi {
    override suspend fun fetchArtworkIds(
        url: Url
    ): Either<AppException, PaginatedIds> =
        PaginatedIds(
            next = searchResponse.next?.id,
            ids = searchResponse.items.map { it.id }
        ).right()

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        Either.Left(exception)
}

private fun createSearchResponse(
    vararg ids: Url
) = HumanMadeObjectResponse.ArtworksResponse(
    next = HumanMadeObjectResponse.NextPage(
        id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2")
    ),
    items = ids.map { HumanMadeObjectResponse.ArtworkIdItem(id = it) }
)
