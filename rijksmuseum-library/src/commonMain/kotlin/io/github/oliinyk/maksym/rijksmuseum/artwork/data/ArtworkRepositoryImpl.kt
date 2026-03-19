package io.github.oliinyk.maksym.rijksmuseum.artwork.data

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApi
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

internal class ArtworkRepositoryImpl(
    private val api: SearchApi,
    private val cache: ValueHolder<Artwork>,
) : ArtworkRepository {
    override suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> {
        return cache.getAndForget()?.right() ?: api.fetchDetails(url)
    }
}
