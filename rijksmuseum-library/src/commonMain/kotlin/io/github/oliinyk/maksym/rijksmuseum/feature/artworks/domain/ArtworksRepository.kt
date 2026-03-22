package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Page
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Paging

/**
 * Domain-level interface defining the search operations available to the application.
 */
public interface ArtworksRepository {
    /**
     * Fetches a page of artworks according to the given [paging] state.
     *
     * @param paging Describes how many items have already been loaded and how many to fetch next.
     * @return [arrow.core.Either.Right] with a [Page] of [Artwork] items on success, or [arrow.core.Either.Left] with an [AppException] on failure.
     */
    public suspend fun fetchArtworks(paging: Paging): Either<AppException, Page<Artwork>>
}
