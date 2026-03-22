package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Page
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Paging

/**
 * Use case class responsible for orchestrating search operations.
 * This class coordinates requests between the domain layer and the [SearchRepository].
 */
public class SearchUseCase(
    private val searchRepository: SearchRepository
) {
    /**
     * Executes an artwork search with the specified [paging] configuration.
     */
    public suspend fun searchArtworks(paging: Paging): Either<AppException, Page<Artwork>> {
        return searchRepository.fetchArtworks(paging)
    }
}
