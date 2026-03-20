package io.github.oliinyk.maksym.rijksmuseum.artwork.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepository
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

public class GetArtworkUseCase(
    private val artworkRepository: ArtworkRepository
) {
    public suspend fun getArtwork(url: Url): Either<AppException, Artwork> {
        return artworkRepository.fetchArtworkDetails(url)
    }
}
