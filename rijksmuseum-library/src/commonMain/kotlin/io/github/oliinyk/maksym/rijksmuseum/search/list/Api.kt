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
        client.safeRequest { get(url.toExternalValue()).body<SearchResponse>() }

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        client.safeRequest {
            println("Fetching details for $url")
            val humanMadeObject = client.get(url.toExternalValue()).body<HumanMadeObjectResponse>()
            val name = humanMadeObject.identifiedBy.filter { it.type == "Name" }
                .firstNotNullOfOrNull { it.content }
                ?: error("No name found for ${humanMadeObject.id}")
            val visualItemDetails = humanMadeObject.shows.firstNotNullOfOrNull {
                client.get(it.id).body<VisualItemDetails>()
            } ?: error("No visual item details found for ${humanMadeObject.id}")
            val digitalObjectDetails =
                visualItemDetails.digitallyShownBy.firstNotNullOfOrNull {
                    client.get(it.id).body<DigitalObjectDetails>()
                }
                    ?: error("No digital object details found for ${visualItemDetails.id}")
            val urls = digitalObjectDetails.accessPoint.map { it.id }

            Artwork(
                url = url,
                title = Title(name),
                images = urls.map { UrlFrom(it) }
            )
        }
}

private suspend inline fun <T> HttpClient.safeRequest(
    request: suspend HttpClient.() -> T
): Either<AppException, T> = Either.catch { request() }.mapLeft { it }