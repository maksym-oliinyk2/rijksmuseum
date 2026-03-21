package io.github.oliinyk.maksym.rijksmuseum.artworks.list

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

internal class TestRijksmuseumApi(
    private val artworksDetails: Map<Url, Either<AppException, Artwork>>,
    private val searchResponses: Map<Url, Either<AppException, PaginatedIds>> = emptyMap(),
) : RijksmuseumApi {
    override suspend fun fetchArtworkIds(page: Url): Either<AppException, PaginatedIds> =
        searchResponses[page] ?: error("No search response for $page")

    override suspend fun fetchArtwork(url: Url): Either<AppException, Artwork> =
        artworksDetails[url] ?: error("No artwork for $url")
}
