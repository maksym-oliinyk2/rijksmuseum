package io.github.oliinyk.maksym.rijksmuseum.search.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

/**
 * Domain-level interface defining the search operations available to the application.
 */
public interface SearchRepository {
    public suspend fun fetchArtworks(paging: Paging): Either<AppException, Page<Artwork>>
    public suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork>
}
