package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Page
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Paging

/**
 * Use case class responsible for orchestrating search operations.
 * This class coordinates requests between the domain layer and the [ArtworksRepository].
 */
public class ArtworksUseCase(
    private val artworksRepository: ArtworksRepository
) {
    /**
     * Executes an artwork search with the specified [paging] configuration.
     *
     * @param paging Describes how many items have already been loaded and how many to fetch next.
     * @return [arrow.core.Either.Right] with a [Page] of [Artwork] items on success, or [arrow.core.Either.Left] with an [AppException] on failure.
     */
    public suspend fun fetchArtworks(paging: Paging): Either<AppException, Page<Artwork>> {
        return artworksRepository.fetchArtworks(paging)
    }
}
