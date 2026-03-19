package io.github.oliinyk.maksym.rijksmuseum.artworks.list

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.ArtworkPreview
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue

internal class TestSearchApi(
    private val artworksDetails: Map<String, ArtworkPreview>,
    private val searchResponses: Map<Url, SearchResponse> = emptyMap(),
) : SearchApi {
    override suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse> =
        searchResponses[url]?.right() ?: error("No search response for $url")

    override suspend fun fetchDetails(url: Url): Either<AppException, ArtworkPreview> =
        artworksDetails[url.toExternalValue()]?.right() ?: error("No artwork for $url")
}
