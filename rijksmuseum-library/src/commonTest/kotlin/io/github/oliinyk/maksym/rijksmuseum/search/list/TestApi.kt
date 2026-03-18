package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue

internal class TestApi(
    private val artworksDetails: Map<String, Artwork>,
    private val searchResponses: Map<Url, SearchResponse> = emptyMap(),
) : Api {
    override suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse> =
        searchResponses[url]?.right() ?: error("No search response for $url")

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        artworksDetails[url.toExternalValue()]?.right() ?: error("No artwork for $url")
}