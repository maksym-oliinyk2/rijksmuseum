package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import arrow.core.raise.either
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val api: RijksmuseumApiImpl,
) : SearchRepository {

    override suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> = TODO()

    private var cachedSearchResponse: SearchResponse? = null

    override suspend fun searchArtworks(paging: Paging): Either<AppException, Page<Artwork>> =
        either {
            coroutineScope {
                val currentCachedResponse = cachedSearchResponse

                val page: Page<Artwork> = when {
                    // no cache hit
                    currentCachedResponse == null -> {
                        val curr = api.searchArtworks(SearchUrl).bind().also {
                            cachedSearchResponse = it
                        }
                        // oh come on, 100 items should be enough for everyone
                        require(curr.orderedItems.size >= paging.currentSize + paging.resultsPerPage) {
                            "Too large page size"
                        }

                        val itemsToGrab = curr.orderedItems
                            .subList(paging.currentSize, paging.currentSize + paging.resultsPerPage)

                        val itemsAsync =
                            itemsToGrab.map { async { api.fetchDetails(UrlFrom(it.id)).bind() } }
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

                        val itemsAsync =
                            itemsToGrab.map { async { api.fetchDetails(UrlFrom(it.id)).bind() } }
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
                            api.searchArtworks(UrlFrom(currentCachedResponse.next.id)).bind()
                                .also { cachedSearchResponse = it }
                        val prevAsync =
                            fromPreviousPage.map {
                                async {
                                    api.fetchDetails(UrlFrom(it.id)).bind()
                                }
                            }
                        val currAsync =
                            newPage.orderedItems.takeLast(paging.resultsPerPage - fromPreviousPage.size)
                                .map { async { api.fetchDetails(UrlFrom(it.id)).bind() } }

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
}


