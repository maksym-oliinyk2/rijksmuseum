package io.github.oliinyk.maksym.rijksmuseum.core.data

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url

internal data class PaginatedIds(
    val next: Url?,
    val ids: List<Url>,
)

internal interface RijksmuseumApi {

    suspend fun fetchArtworkIds(page: Url): Either<AppException, PaginatedIds>

    suspend fun fetchArtwork(url: Url): Either<AppException, Artwork>
}

