package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.DigitalObjectDetails
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.VisualItemDetails
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal val SearchUrl = UrlFrom("https://data.rijksmuseum.nl/search/collection")

internal interface RijksmuseumApi {
    suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse>

    suspend fun fetchDetails(url: Url): Either<AppException, Artwork>
}

internal class RijksmuseumApiImpl(
    private val client: HttpClient,
) : RijksmuseumApi {
    override suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse> =
        Either.catch {
            client.get(url.toExternalValue()).body<SearchResponse>()
        }

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        Either.catch {
            println("Fetching details for $url")
            val humanMadeObject1 =
                client.get(url.toExternalValue()).body<HumanMadeObjectResponse>()
            val name1 = humanMadeObject1.identifiedBy.filter { it.type == "Name" }
                .firstNotNullOfOrNull { it.content }
                ?: error("No name found for ${humanMadeObject1.id}")
            val visualItemDetails1 = humanMadeObject1.shows.firstNotNullOfOrNull {
                client.get(it.id).body<VisualItemDetails>()
            } ?: error("No visual item details found for ${humanMadeObject1.id}")
            val digitalObjectDetails1 =
                visualItemDetails1.digitallyShownBy.firstNotNullOfOrNull {
                    client.get(it.id).body<DigitalObjectDetails>()
                }
                    ?: error("No digital object details found for ${visualItemDetails1.id}")
            val urls1 = digitalObjectDetails1.accessPoint.map { it.id }
            Artwork(
                url = url,
                title = Title(name1),
                images = urls1.map { UrlFrom(it) }
            )
        }
}
