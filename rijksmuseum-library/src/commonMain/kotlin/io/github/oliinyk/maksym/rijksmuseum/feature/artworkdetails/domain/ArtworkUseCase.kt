package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url

/**
 * Use case that retrieves the full details of a single artwork by its [Url].
 *
 * Delegates to [ArtworkRepository] and returns either the [Artwork] or an [io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException]
 * on failure.
 */
public class ArtworkUseCase(
    private val artworkRepository: ArtworkRepository
) {
    /**
     * Fetches the artwork identified by [url].
     *
     * @param url The canonical URL of the artwork in the Rijksmuseum Linked Data API.
     * @return [arrow.core.Either.Right] with the [Artwork] on success, or [arrow.core.Either.Left] with an [AppException] on failure.
     */
    public suspend fun fetchArtwork(url: Url): Either<AppException, Artwork> {
        return artworkRepository.fetchArtworkDetails(url)
    }
}
