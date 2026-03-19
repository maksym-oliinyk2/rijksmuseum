package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Page
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging

/**
 * Domain-level interface defining the search operations available to the application.
 */
public interface SearchRepository {
    public suspend fun fetchArtworks(paging: Paging): Either<AppException, Page<Artwork>>
    public suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork>
}
