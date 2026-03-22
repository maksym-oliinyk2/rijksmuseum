package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url

public interface ArtworkRepository {
    public suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork>
}
