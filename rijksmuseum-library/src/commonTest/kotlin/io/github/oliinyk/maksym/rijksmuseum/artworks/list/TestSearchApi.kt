package io.github.oliinyk.maksym.rijksmuseum.artworks.list

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.ArtworksResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.ArtworkPreview
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

internal class TestSearchApi(
    private val artworksDetails: Map<Url, ArtworkPreview>,
    private val searchResponses: Map<Url, ArtworksResponse> = emptyMap(),
) : SearchApi {
    override suspend fun search(url: Url): Either<AppException, ArtworksResponse> =
        searchResponses[url]?.right() ?: error("No search response for $url")

    override suspend fun fetchDetails(url: Url): Either<AppException, ArtworkPreview> =
        artworksDetails[url]?.right() ?: error("No artwork for $url")
}
