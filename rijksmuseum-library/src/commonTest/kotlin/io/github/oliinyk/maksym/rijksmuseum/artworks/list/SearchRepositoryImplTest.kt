package io.github.oliinyk.maksym.rijksmuseum.artworks.list

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchUrl
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Page
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class SearchRepositoryImplTest {

    @Test
    fun `when fetchArtworks and cache is empty then it fetches from api`() = runTest {
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
            primaryImage = UrlFrom("https://lh3.googleusercontent.com/NF7Z_E-S_6e-M-p8Bf8Bf8B"),
            descriptions = listOf(),
        )
        val artwork2 = Artwork(
            url = item2Id,
            title = Title("The Milkmaid"),
            primaryImage = UrlFrom("https://lh3.googleusercontent.com/c6_9-f1_y-p8Bf8Bf8Bf8B"),
            descriptions = listOf(),
        )

        // Fill the details for all items to avoid errors during parMap
        val artworksDetails = searchResponse.items.associate { item ->
            item.id to Artwork(
                url = item.id,
                title = Title("Title for ${item.id}"),
                primaryImage = UrlFrom("https://image.url/${item.id}"),
                descriptions = listOf(),
            )
        }.toMutableMap()

        artworksDetails[item1Id] = artwork1
        artworksDetails[item2Id] = artwork2

        val api = TestSearchApi(
            artworksDetails = artworksDetails.mapValues { it.value.right() },
            searchResponses = mapOf(
                SearchUrl to PaginatedIds(
                    next = searchResponse.next?.id,
                    ids = searchResponse.items.map { it.id }
                ).right()
            )
        )
        val repository = SearchRepositoryImpl(api)

        // Execute
        val paging = Paging(currentSize = 0, resultsPerPage = 2)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertEquals(Page(hasMore = true, data = listOf(artwork1, artwork2)), page)
    }

    @Test
    fun `when fetchArtworks and cache hit with enough items then it returns cached data`() = runTest {
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
                primaryImage = UrlFrom("https://image.url/${item.id}"),
                descriptions = listOf(),
            )
        }

        val api = TestSearchApi(
            artworksDetails = artworksDetails.mapValues { it.value.right() },
            searchResponses = mapOf(
                SearchUrl to PaginatedIds(
                    next = initialSearchResponse.next?.id,
                    ids = initialSearchResponse.items.map { it.id }
                ).right()
            )
        )
        val repository = SearchRepositoryImpl(api)

        // Execute: Ask for 2 items starting from index 0
        val paging = Paging(currentSize = 0, resultsPerPage = 2)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertEquals(Page(hasMore = true, data = artworksDetails.values.take(paging.resultsPerPage).toList()), page)
    }

    @Test
    fun `when fetchArtworks and cache hit and it is end of pagination then it returns end of page`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")

        val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
        )

        val api = TestSearchApi(
            artworksDetails = mapOf(),
            searchResponses = mapOf(
                SearchUrl to PaginatedIds(
                    next = initialSearchResponse.next?.id,
                    ids = initialSearchResponse.items.map { it.id }
                ).right()
            )
        )
        val repository = SearchRepositoryImpl(
            api = api,
            cachedIds = listOf(item1Id),
            startUrl = null
        )

        // Execute: currentSize = 1, resultsPerPage = 1. items.size = 1.
        val paging = Paging(currentSize = 1, resultsPerPage = 1)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertSame(Page.End, page as Page<*>)
    }

    @Test
    fun `when fetchArtworks and cache hit but not enough items then it fetches next page`() =
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

            val artworksDetails = mapOf(
                item1Id to Artwork(
                    url = item1Id,
                    title = Title("Title for $item1Id"),
                    primaryImage = UrlFrom("https://image.url/item-1"),
                    descriptions = listOf()
                ),
                item2Id to Artwork(
                    url = item2Id,
                    title = Title("Title for $item2Id"),
                    primaryImage = UrlFrom("https://image.url/item-2"),
                    descriptions = listOf()
                ),
                item3Id to Artwork(
                    url = item3Id,
                    title = Title("Title for $item3Id"),
                    primaryImage = UrlFrom("https://image.url/item-3"),
                    descriptions = listOf()
                )
            )

            val api = TestSearchApi(
                artworksDetails = artworksDetails.mapValues { it.value.right() },
                searchResponses = mapOf(
                    SearchUrl to PaginatedIds(
                        next = initialSearchResponse.next?.id,
                        ids = initialSearchResponse.items.map { it.id }
                    ).right(),
                    UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2") to PaginatedIds(
                        next = nextSearchResponse.next?.id,
                        ids = nextSearchResponse.items.map { it.id }
                    ).right()
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
            assertEquals(Page(hasMore = true, data = artworksDetails.values.take(paging.resultsPerPage).toList()), page)
        }

    @Test
    fun `when fetchArtworks and request is beyond cache and no more pages then it returns end of page`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
        val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
        )
        val api = TestSearchApi(
            artworksDetails = mapOf(),
            searchResponses = mapOf(
                SearchUrl to PaginatedIds(
                    next = initialSearchResponse.next?.id,
                    ids = initialSearchResponse.items.map { it.id }
                ).right()
            )
        )
        val repository = SearchRepositoryImpl(
            api = api,
            cachedIds = listOf(item1Id),
            startUrl = null
        )

        // Execute: Ask for items at index 5, but only 1 exists
        val paging = Paging(currentSize = 5, resultsPerPage = 2)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertSame(Page.End, page as Page<*>)
    }

    @Test
    fun `when fetchArtworks and multiple pages need to be fetched then it fetches them all`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
        val item2Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-2")
        val item3Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-3")

        val artworksDetails = mapOf(
            item1Id to Artwork(
                url = item1Id,
                title = Title("Title for $item1Id"),
                primaryImage = UrlFrom("https://image.url/item-1"),
                descriptions = listOf()
            ),
            item2Id to Artwork(
                url = item2Id,
                title = Title("Title for $item2Id"),
                primaryImage = UrlFrom("https://image.url/item-2"),
                descriptions = listOf()
            ),
            item3Id to Artwork(
                url = item3Id,
                title = Title("Title for $item3Id"),
                primaryImage = UrlFrom("https://image.url/item-3"),
                descriptions = listOf()
            )
        )

        val api = TestSearchApi(
            artworksDetails = artworksDetails.mapValues { it.value.right() },
            searchResponses = mapOf(
                SearchUrl to PaginatedIds(
                    next = UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2"),
                    ids = listOf(item1Id)
                ).right(),
                UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2") to PaginatedIds(
                    next = UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=3"),
                    ids = listOf(item2Id)
                ).right(),
                UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=3") to PaginatedIds(
                    next = null,
                    ids = listOf(item3Id)
                ).right()
            )
        )
        val repository = SearchRepositoryImpl(api)

        // Execute: resultsPerPage = 3. Cache only has 1 initially. Need to fetch page-2 and page-3.
        val paging = Paging(currentSize = 0, resultsPerPage = 3)
        val page = repository.fetchArtworks(paging).getOrNull()

        // Verify
        assertEquals(Page(hasMore = false, data = artworksDetails.values.toList()), page)
    }

    @Test
    fun `when fetchArtworks and fetching details fails then it returns failure`() = runTest {
        // Setup
        val item1Id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/item-1")
        val initialSearchResponse = HumanMadeObjectResponse.ArtworksResponse(
            next = null,
            items = listOf(HumanMadeObjectResponse.ArtworkIdItem(id = item1Id))
        )

        val exception = AppException("Network error")
        val api = TestSearchApi(
            artworksDetails = mapOf(item1Id to Either.Left(exception)),
            searchResponses = mapOf(
                SearchUrl to PaginatedIds(
                    next = initialSearchResponse.next?.id,
                    ids = initialSearchResponse.items.map { it.id }
                ).right()
            )
        )
        val repository = SearchRepositoryImpl(api)

        // Execute
        val paging = Paging(currentSize = 0, resultsPerPage = 1)
        val result = repository.fetchArtworks(paging)

        // Verify
        assertEquals(Either.Left(exception), result)
    }

    @Test
    fun `when fetchArtworks and fetching ids fails then it returns failure`() = runTest {
        // Setup
        val exception = AppException("Network error")
        val api = TestSearchApi(
            artworksDetails = mapOf(),
            searchResponses = mapOf(SearchUrl to Either.Left(exception))
        )
        val repository = SearchRepositoryImpl(api)

        // Execute
        val paging = Paging(currentSize = 0, resultsPerPage = 1)
        val result = repository.fetchArtworks(paging)

        // Verify
        assertEquals(Either.Left(exception), result)
    }

    @Test
    fun `when hasMore and exactly at the end of items and no next page then it returns false`() = runTest {
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
                primaryImage = null,
                descriptions = listOf()
            )
        )
        val api = TestSearchApi(
            artworksDetails = artworksDetails.mapValues { it.value.right() },
            searchResponses = mapOf(
                SearchUrl to PaginatedIds(
                    next = initialSearchResponse.next?.id,
                    ids = initialSearchResponse.items.map { it.id }
                ).right()
            )
        )
        val repository = SearchRepositoryImpl(
            api = api,
            cachedIds = listOf(item1Id),
            startUrl = null
        )

        // Execute: currentSize = 0, resultsPerPage = 1. items.size = 1.
        // limit = 0 + 1 = 1.
        // cachedIds.size (1) > limit (1) is false. nextUrl is null.
        val hasMore = repository.fetchArtworks(Paging(currentSize = 0, resultsPerPage = 1))
            .getOrNull()?.hasMore

        // Verify
        assertNotEquals(hasMore, true)
    }
}

private fun createSearchResponse(
    vararg ids: Url
) = HumanMadeObjectResponse.ArtworksResponse(
    next = HumanMadeObjectResponse.NextPage(
        id = UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2")
    ),
    items = ids.map { HumanMadeObjectResponse.ArtworkIdItem(id = it) }
)
