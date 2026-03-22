package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url

/**
 * Repository interface for fetching artwork details in the artwork-details feature.
 */
public interface ArtworkRepository {
    /**
     * Fetches the full details of the artwork identified by [url].
     *
     * @param url The canonical URL of the artwork.
     * @return [arrow.core.Either.Right] with the [Artwork] on success, or [arrow.core.Either.Left] with an [AppException] on failure.
     */
    public suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork>
}
