package io.github.oliinyk.maksym.rijksmuseum.artworks.list

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApi
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

internal class TestSearchApi(
    private val artworksDetails: Map<Url, Either<AppException, Artwork>>,
    private val searchResponses: Map<Url, Either<AppException, PaginatedIds>> = emptyMap(),
) : SearchApi {
    override suspend fun fetchArtworkIds(url: Url): Either<AppException, PaginatedIds> =
        searchResponses[url] ?: error("No search response for $url")

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        artworksDetails[url] ?: error("No artwork for $url")
}
