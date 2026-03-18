package io.github.oliinyk.maksym.rijksmuseum.search.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

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

    /**
     * Fetches detailed information for a specific artwork by its [url].
     */
    public suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> {
        return searchRepository.fetchArtworkDetails(url)
    }
}
