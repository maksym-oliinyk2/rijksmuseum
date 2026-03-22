package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.data

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.ArtworkRepository

internal class ArtworkRepositoryImpl(
    private val api: RijksmuseumApi,
) : ArtworkRepository {
    override suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> {
        return api.fetchArtwork(url)
    }
}
