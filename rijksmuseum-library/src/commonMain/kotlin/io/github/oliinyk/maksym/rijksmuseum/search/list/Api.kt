package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.DigitalObjectDetails
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.VisualItemDetails
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.github.oliinyk.maksym.rijksmuseum.search.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.search.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.search.domain.Title
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal val SearchUrl = UrlFrom("https://data.rijksmuseum.nl/search/collection")

internal interface Api {
    suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse>

    suspend fun fetchDetails(url: Url): Either<AppException, Artwork>
}

internal class ApiImpl(
    private val client: HttpClient,
) : Api {
    override suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse> =
        Either.catch {
            client.get(url.toExternalValue()).body<SearchResponse>()
        }

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        Either.catch {
            // todo come back to this later and refactor
            // 1. Fetch the main artwork object
            val humanMadeObject1 =
                client.get(url.toExternalValue()).body<HumanMadeObjectResponse>()

            // 2. Extract the name (title) from identification fields
            val name = humanMadeObject1.identifiedBy.filter { it.type == "Name" }
                .firstNotNullOfOrNull { it.content }
                ?: error("No name found for ${humanMadeObject1.id}")

            // 3. Fetch visual item details to get links to digital objects (images)
            val visualItemDetails1 = humanMadeObject1.shows.firstNotNullOfOrNull {
                client.get(it.id).body<VisualItemDetails>()
            } ?: error("No visual item details found for ${humanMadeObject1.id}")

            // 4. Fetch digital object details to get the actual access points (image URLs)
            val digitalObjectDetails1 =
                visualItemDetails1.digitallyShownBy.firstNotNullOfOrNull {
                    client.get(it.id).body<DigitalObjectDetails>()
                }
                    ?: error("No digital object details found for ${visualItemDetails1.id}")

            val urls = digitalObjectDetails1.accessPoint.map { it.id }

            Artwork(
                url = url,
                title = Title(name),
                images = urls.map { UrlFrom(it) }
            )
        }
}
