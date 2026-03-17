package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.DigitalObjectDetails
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.VisualItemDetails
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.http.buildUrl
import io.ktor.http.path
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlin.jvm.JvmInline

// extract
public typealias AppException = Throwable

@JvmInline
public value class Title internal constructor(
    public val value: String
) {
    init {
        require(value.isNotEmpty()) { "Title cannot be empty" }
    }
}

public data class Artwork internal constructor(
    val url: Url,
    val title: Title,
    val images: List<Url>
)

internal class SearchUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend fun searchArtworks(paging: Paging): Either<AppException, Page<Artwork>> {
        return searchRepository.searchArtworks(paging)
    }

    suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> {
        return searchRepository.fetchArtworkDetails(url)
    }
}

// extract

internal interface SearchRepository {
    suspend fun searchArtworks(paging: Paging): Either<AppException, Page<Artwork>>
    suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork>
}

internal class SearchRepositoryImpl(
    // in this case hiding the API behind the interface looks like overkill to me,
    // so I'll use a pre-configured http client
    private val client: HttpClient,
) : SearchRepository {

    override suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> = TODO()

    /*
            client.safeRequest {
                val humanMadeObject = get(url.toExternalValue()).body<HumanMadeObjectResponse>()
                val name = humanMadeObject.identifiedBy.filter { it.type == "Name" }
                    .firstNotNullOfOrNull { it.content } ?: error("name not found")
                val visualItemDetails = humanMadeObject.shows.firstNotNullOfOrNull {
                    get(it.id).body<VisualItemDetails>()
                } ?: error("visualItemDetails")

                val digitalObjectDetails =
                    visualItemDetails.digitallyShownBy.firstNotNullOfOrNull { get(it.id).body<DigitalObjectDetails>() }
                        ?: error("digitalObjectDetails details not found")
                val urls = digitalObjectDetails.accessPoint.map { UrlFrom(it.id) }

                Artwork(url, Title(name), urls)
            }
    */
    private var cachedSearchResponse: SearchResponse? = null


    override suspend fun searchArtworks(paging: Paging): Either<AppException, Page<Artwork>> =
        client.safeRequest {

            val currentCachedResponse = cachedSearchResponse

            val page: Page<Artwork> = when {
                // no cache hit
                currentCachedResponse == null -> {
                    val curr = get(SearchUrl).body<SearchResponse>().also {
                        cachedSearchResponse = it
                    }
                    // oh come on, 100 items should be enough for everyone
                    require(curr.orderedItems.size >= paging.currentSize + paging.resultsPerPage) {
                        "Too large page size"
                    }

                    val itemsToGrab = curr.orderedItems
                        .subList(paging.currentSize, paging.currentSize + paging.resultsPerPage)

                    val itemsAsync = itemsToGrab.map { async { fetchDetails(UrlFrom(it.id)) } }
                    val items = itemsAsync.awaitAll()

                    Page(
                        hasMore = curr.orderedItems.size - paging.currentSize - paging.resultsPerPage > 0 || curr.next != null,
                        data = items
                    )
                }
                // there is a cached response and it can provide enough items
                currentCachedResponse.orderedItems.size >= paging.currentSize + paging.resultsPerPage -> {
                    val itemsToGrab = currentCachedResponse.orderedItems
                        .subList(paging.currentSize, paging.currentSize + paging.resultsPerPage)

                    val itemsAsync = itemsToGrab.map { async { fetchDetails(UrlFrom(it.id)) } }
                    val items = itemsAsync.awaitAll()

                    Page(
                        hasMore = currentCachedResponse.orderedItems.size - paging.currentSize - paging.resultsPerPage > 0 || currentCachedResponse.next != null,
                        data = items
                    )
                }
                // this unchecked case never fails
                currentCachedResponse.next == null -> @Suppress("UNCHECKED_CAST") (Page.End as Page<Artwork>)
                else -> {
                    val fromPreviousPage =
                        if (currentCachedResponse.orderedItems.size > paging.currentSize) {
                            // cached response can provide some items but not all
                            currentCachedResponse.orderedItems.takeLast(currentCachedResponse.orderedItems.size - paging.currentSize)
                    } else {
                            listOf()
                    }
                    val newPage =
                        get(currentCachedResponse.next.id).body<SearchResponse>()
                            .also { cachedSearchResponse = it }
                    val prevAsync = fromPreviousPage.map { async { fetchDetails(UrlFrom(it.id)) } }
                    val currAsync =
                        newPage.orderedItems.takeLast(paging.resultsPerPage - fromPreviousPage.size)
                            .map { async { fetchDetails(UrlFrom(it.id)) } }

                    val prev = prevAsync.awaitAll()
                    val curr = currAsync.awaitAll()

                    Page(
                        hasMore = newPage.orderedItems.size - curr.size > 0 || newPage.next != null,
                        data = prev + curr
                    )
                }
            }

            page
        }
}

private suspend fun HttpClient.fetchDetails(url: Url): Artwork {
    println("Fetching details for $url")
    val humanMadeObject = get(url.toExternalValue()).body<HumanMadeObjectResponse>()
    val name = humanMadeObject.identifiedBy.filter { it.type == "Name" }
        .firstNotNullOfOrNull { it.content } ?: error("No name found for ${humanMadeObject.id}")
    val visualItemDetails = humanMadeObject.shows.firstNotNullOfOrNull {
        get(it.id).body<VisualItemDetails>()
    } ?: error("No visual item details found for ${humanMadeObject.id}")

    val digitalObjectDetails =
        visualItemDetails.digitallyShownBy.firstNotNullOfOrNull { get(it.id).body<DigitalObjectDetails>() }
            ?: error("No digital object details found for ${visualItemDetails.id}")
    val urls = digitalObjectDetails.accessPoint.map { it.id }

    return Artwork(
        url = url,
        title = Title(name),
        images = urls.map { UrlFrom(it) }
    )
}

/*for (item in all.orderedItems) {
                println("checking: ${item.id}")
                val humanMadeObject = get(item.id).body<HumanMadeObjectResponse>()
                val name = humanMadeObject.identifiedBy.filter { it.type == "Name" }.firstNotNullOfOrNull { it.content } ?: continue
                val visualItemDetails = humanMadeObject.shows.firstNotNullOfOrNull {
                    get(it.id).body<VisualItemDetails>()
                } ?: continue

                val digitalObjectDetails =
                    visualItemDetails.digitallyShownBy.firstNotNullOfOrNull { get(it.id).body<DigitalObjectDetails>() }
                        ?: continue
                val urls = digitalObjectDetails.accessPoint.map { it.id }

                println("FOUND URLS for ${item.id} $urls")
            }*/
private val SearchUrl = buildUrl {
    protocol = HTTPS
    host = "data.rijksmuseum.nl"
    path("search", "collection")
}

private fun DetailsUrl(id: String) = buildUrl {
    protocol = HTTPS
    host = "id.rijksmuseum.nl"
    path(id)
}

/*
internal class RijksmuseumApiImpl(
    private val client: HttpClient,
) : RijksmuseumApi {
    override suspend fun searchArtworks(page: Url?): Either<AppException, SearchResponse> =

}
*/


internal suspend inline fun <T> HttpClient.safeRequest(
    request: suspend HttpClient.() -> T
): Either<AppException, T> = Either.catch { request() }.mapLeft { it }
