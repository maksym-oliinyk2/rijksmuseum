package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parMap
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Page
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SearchRepositoryImpl(
    private val api: SearchApi,
    cachedIds: List<Url> = emptyList(),
    startUrl: Url? = SearchUrl,
) : SearchRepository {
    // only one coroutine at a time can access the cache
    private val cachedIds = cachedIds.toMutableList()
    private var nextPage: Url? = startUrl
    private val mutex = Mutex()

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
        either {
            val limit = paging.currentSize + paging.resultsPerPage
            // Incrementally fetch next pages until we have enough ids in the cache
            var currentUrl = nextPage
            while (cachedIds.size < limit && currentUrl != null) {
                val (next, ids) = api.fetchArtworkIds(currentUrl).bind()
                cachedIds.addAll(ids)
                currentUrl = next
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
