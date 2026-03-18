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

/**
 * Interface representing the Rijksmuseum API for searching artworks and fetching their details.
 */
internal interface Api {
    /**
     * Searches for artworks and returns a [SearchResponse] containing IDs and the next page URL.
     */
    suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse>

    /**
     * Fetches full details for a specific artwork by its [url] and returns an [Artwork] object.
     */
    suspend fun fetchDetails(url: Url): Either<AppException, Artwork>
}

/**
 * Implementation of [Api] using Ktor [HttpClient].
 * This implementation extracts artwork details (title and images) by traversing the linked
 * data structure provided by the Rijksmuseum API.
 */
internal class ApiImpl(
    private val client: HttpClient,
) : Api {
    override suspend fun searchArtworks(url: Url): Either<AppException, SearchResponse> =
        Either.catch {
            client.get(url.toExternalValue()).body<SearchResponse>()
        }

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        Either.catch {
            // 1. Fetch the main artwork object
            val humanMadeObject1 =
                client.get(url.toExternalValue()).body<HumanMadeObjectResponse>()

            // 2. Extract the name (title) from identification fields
            val name1 = humanMadeObject1.identifiedBy.filter { it.type == "Name" }
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

            val urls1 = digitalObjectDetails1.accessPoint.map { it.id }

            Artwork(
                url = url,
                title = Title(name1),
                images = urls1.map { UrlFrom(it) }
            )
        }
}
