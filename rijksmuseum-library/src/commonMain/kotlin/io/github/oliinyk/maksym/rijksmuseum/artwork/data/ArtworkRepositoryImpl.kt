package io.github.oliinyk.maksym.rijksmuseum.artwork.data

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ArtworkRepositoryImpl(
    private val api: RijksmuseumApi,
    private val cache: ValueHolder<Artwork>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ArtworkRepository {
    override suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> {
        return withContext(dispatcher) { cache.getAndForget() }?.right() ?: api.fetchArtwork(url)
    }
}
