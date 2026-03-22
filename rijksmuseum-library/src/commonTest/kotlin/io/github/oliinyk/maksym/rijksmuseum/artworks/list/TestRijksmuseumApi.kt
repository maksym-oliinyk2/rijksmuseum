package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.data.PaginatedIds

..list

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.core.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url

internal class TestRijksmuseumApi(
    private val artworksDetails: Map<Url, Either<AppException, Artwork>>,
    private val searchResponses: Map<Url, Either<AppException, PaginatedIds>> = emptyMap(),
) : RijksmuseumApi {
    override suspend fun fetchArtworkIds(page: Url): Either<AppException, PaginatedIds> =
        searchResponses[page] ?: error("No search response for $page")

    override suspend fun fetchArtwork(url: Url): Either<AppException, Artwork> =
        artworksDetails[url] ?: error("No artwork for $url")
}
