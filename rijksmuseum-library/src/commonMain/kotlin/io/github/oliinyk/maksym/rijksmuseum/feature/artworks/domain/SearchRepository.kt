package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Page
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Paging

/**
 * Domain-level interface defining the search operations available to the application.
 */
public interface SearchRepository {
    public suspend fun fetchArtworks(paging: Paging): Either<AppException, Page<Artwork>>
}
