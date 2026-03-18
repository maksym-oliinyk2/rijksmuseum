package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.fx.coroutines.parMap
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
        return searchRepository.fetchArtworks(paging)
    }

    suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> {
        return searchRepository.fetchArtworkDetails(url)
    }
}

// extract

internal interface SearchRepository {
    suspend fun fetchArtworks(paging: Paging): Either<AppException, Page<Artwork>>
    suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork>
}

internal class SearchRepositoryImpl(
    // in this case hiding the API behind the interface looks like overkill to me,
    // so I'll use a pre-configured http client
    private val api: RijksmuseumApiImpl,
) : SearchRepository {

    override suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> = TODO()

    private val searchResponseCache = InMemoryCache<SearchResponse>()

    override suspend fun fetchArtworks(paging: Paging): Either<AppException, Page<Artwork>> =
        either {
            // if another coroutine is updating the cache, we suspend here
            val currentCachedResponse = searchResponseCache.get()
            val page: Page<Artwork> = when {
                // no cache hit
                currentCachedResponse == null -> {
                    val curr = searchResponseCache.update { api.searchArtworks(SearchUrl).bind() }
                    // oh come on, 100 items should be enough for everyone
                    ensure(curr.orderedItems.size >= paging.currentSize + paging.resultsPerPage) {
                        IllegalArgumentException("Too large page size")
                    }

                    val itemsToGrab = curr.orderedItems
                        .subList(paging.currentSize, paging.currentSize + paging.resultsPerPage)

                    val artworks = itemsToGrab.parMap { item ->
                        api.fetchDetails(UrlFrom(item.id)).bind()
                    }

                    Page(
                        hasMore = curr.orderedItems.size - paging.currentSize - paging.resultsPerPage > 0 || curr.next != null,
                        data = artworks
                    )
                }
                // there is a cached response and it can provide enough items
                currentCachedResponse.orderedItems.size >= paging.currentSize + paging.resultsPerPage -> {
                    val itemsToGrab = currentCachedResponse.orderedItems
                        .subList(paging.currentSize, paging.currentSize + paging.resultsPerPage)

                    val artworks =
                        itemsToGrab.parMap { api.fetchDetails(UrlFrom(it.id)).bind() }

                    Page(
                        hasMore = currentCachedResponse.orderedItems.size - paging.currentSize - paging.resultsPerPage > 0 || currentCachedResponse.next != null,
                        data = artworks
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
                    val newPage = searchResponseCache.update {
                        api.searchArtworks(
                            UrlFrom(currentCachedResponse.next.id)
                        ).bind()
                    }
                    val prevAsync =
                        fromPreviousPage.parMap { api.fetchDetails(UrlFrom(it.id)).bind() }
                    val currAsync =
                        newPage.orderedItems.takeLast(paging.resultsPerPage - fromPreviousPage.size)
                            .parMap { api.fetchDetails(UrlFrom(it.id)).bind() }

                    Page(
                        hasMore = newPage.orderedItems.size - currAsync.size > 0 || newPage.next != null,
                        data = prevAsync + currAsync
                    )
                }
            }

            page
        }

}

private class InMemoryCache<T> {
    private var cachedSearchResponse: T? = null
    private val mutex = Mutex()

    // if another coroutine is trying to update the cached response, this function suspends
    suspend fun get(): T? = mutex.withLock {
        cachedSearchResponse
    }

    // fetches artworks list response and caches if api response is successful
    suspend fun update(block: suspend () -> T) = mutex.withLock {
        val updated = block()

        cachedSearchResponse = updated

        updated
    }
}
