package io.github.oliinyk.maksym.rijksmuseum.artwork.data

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

public interface ArtworkRepository {
    public suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork>
}
