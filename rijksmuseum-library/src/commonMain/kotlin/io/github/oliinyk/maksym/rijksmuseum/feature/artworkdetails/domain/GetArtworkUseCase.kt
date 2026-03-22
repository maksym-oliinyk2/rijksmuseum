package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url

public class GetArtworkUseCase(
    private val artworkRepository: ArtworkRepository
) {
    public suspend fun getArtwork(url: Url): Either<AppException, Artwork> {
        return artworkRepository.fetchArtworkDetails(url)
    }
}
