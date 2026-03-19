package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Description
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.LinguisticObject as DomainLinguisticObject

// todo it shouldn't be here
internal val SearchUrl = UrlFrom("https://data.rijksmuseum.nl/search/collection")

internal interface SearchApi {
    suspend fun search(url: Url): Either<AppException, HumanMadeObjectResponse.ArtworksResponse>

    suspend fun fetchDetails(url: Url): Either<AppException, Artwork>
}

internal class SearchApiImpl(
    private val client: HttpClient,
) : SearchApi {
    override suspend fun search(url: Url): Either<AppException, HumanMadeObjectResponse.ArtworksResponse> =
        Either.catch {
            client.get(url.toExternalValue()).body<HumanMadeObjectResponse.ArtworksResponse>()
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
                client.get(it.id.toExternalValue()).body<HumanMadeObjectResponse.VisualItemDetails>()
            } ?: error("No visual item details found for ${humanMadeObject1.id}")

            // 4. Fetch digital object details to get the actual access points (image URLs)
            val digitalObjectDetails1 =
                visualItemDetails1.digitallyShownBy.firstNotNullOfOrNull {
                    client.get(it.id.toExternalValue()).body<HumanMadeObjectResponse.DigitalObject>()
                }
                    ?.let {
                        client.get(it.id.toExternalValue()).body<HumanMadeObjectResponse.DigitalObjectDetails>()
                    }
                    ?: error("No digital object details found for ${visualItemDetails1.id}")

            val urls = digitalObjectDetails1.accessPoint.map { it.id }

            val descriptions = humanMadeObject1.toLinguisticObjects()

            Artwork(
                url = url,
                title = Title(name),
                images = urls,
                descriptions = descriptions
            )
        }
}

private fun HumanMadeObjectResponse.toLinguisticObjects(): List<DomainLinguisticObject> =
    referredToBy.mapNotNull { obj ->
        obj.content?.let { content ->
            obj.classifiedAs.firstNotNullOfOrNull { classification ->
                GettyAatType.fromId(classification.id)
                    ?.let { type -> DomainLinguisticObject(type, Description(content)) }
            }
        }
    }
