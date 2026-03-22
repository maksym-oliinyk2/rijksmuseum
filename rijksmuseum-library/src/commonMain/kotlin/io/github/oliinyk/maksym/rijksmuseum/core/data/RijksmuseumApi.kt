package io.github.oliinyk.maksym.rijksmuseum.core.data

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url

/**
 * Holds a page of artwork IDs returned by the Rijksmuseum API, along with the URL of the next page.
 *
 * @property next URL of the next page of results, or `null` if this is the last page.
 * @property ids The list of artwork URLs returned for this page.
 */
internal data class PaginatedIds(
    val next: Url?,
    val ids: List<Url>,
)

/**
 * Low-level HTTP client interface for the Rijksmuseum Linked Data API.
 *
 * Implementations use Ktor to perform network requests and map responses to domain models.
 */
internal interface RijksmuseumApi {

    /**
     * Fetches a page of artwork IDs starting at [page].
     *
     * @param page URL of the page to fetch (use the initial page URL for the first request).
     * @return [arrow.core.Either.Right] with [PaginatedIds] on success, or [arrow.core.Either.Left] with an [AppException] on failure.
     */
    suspend fun fetchArtworkIds(page: Url): Either<AppException, PaginatedIds>

    /**
     * Fetches the full details of a single artwork identified by [url].
     *
     * @param url The canonical URL of the artwork.
     * @return [arrow.core.Either.Right] with the [Artwork] on success, or [arrow.core.Either.Left] with an [AppException] on failure.
     */
    suspend fun fetchArtwork(url: Url): Either<AppException, Artwork>
}
