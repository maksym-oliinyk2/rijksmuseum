package io.github.oliinyk.maksym.rijksmuseum.artworks.list

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApi
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

internal class TestSearchApi(
    private val artworksDetails: Map<Url, Artwork>,
    private val searchResponses: Map<Url, HumanMadeObjectResponse.ArtworksResponse> = emptyMap(),
) : SearchApi {
    override suspend fun search(url: Url): Either<AppException, HumanMadeObjectResponse.ArtworksResponse> =
        searchResponses[url]?.right() ?: error("No search response for $url")

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        artworksDetails[url]?.right() ?: error("No artwork for $url")
}
