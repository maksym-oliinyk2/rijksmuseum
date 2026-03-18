package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import arrow.core.raise.either
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
    private val api: Api,
    cachedIds: List<Url> = emptyList(),
    nextUrl: Url? = SearchUrl,
) : SearchRepository {

    private val cachedIds = cachedIds.toMutableList()
    private var nextPage: Url? = nextUrl
    private val mutex = Mutex()

    override suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> = TODO()

    override suspend fun fetchArtworks(paging: Paging): Either<AppException, Page<Artwork>> =
        either {
            val ids = fetchArtworkIds(paging).bind()

            if (ids.isEmpty() && paging.currentSize > 0) {
                @Suppress("UNCHECKED_CAST")
                Page.End as Page<Artwork>
            } else {
                val artworks = ids.parMap { id ->
                    api.fetchDetails(id).bind()
                }

                Page(
                    hasMore = hasMore(paging),
                    data = artworks
                )
            }
        }

    private suspend fun fetchArtworkIds(
        paging: Paging,
    ): Either<AppException, List<Url>> = mutex.withLock {
        // only one coroutine at a time can access the cache
        either {
            val limit = paging.currentSize + paging.resultsPerPage
            // Incrementally fetch next pages until we have enough ids in the cache
            var currentUrl = nextPage
            while (cachedIds.size < limit && currentUrl != null) {
                val response = api.searchArtworks(currentUrl).bind()
                cachedIds.addAll(response.orderedItems.map { UrlFrom(it.id) })
                currentUrl = response.next?.let { UrlFrom(it.id) }
            }

            nextPage = currentUrl

            if (paging.currentSize >= cachedIds.size) {
                listOf()
            } else {
                cachedIds.subList(
                    paging.currentSize,
                    limit.coerceAtMost(cachedIds.size)
                )
            }
        }
    }

    private suspend fun hasMore(paging: Paging): Boolean = mutex.withLock {
        val limit = paging.currentSize + paging.resultsPerPage
        cachedIds.size > limit || nextPage != null
    }
}
