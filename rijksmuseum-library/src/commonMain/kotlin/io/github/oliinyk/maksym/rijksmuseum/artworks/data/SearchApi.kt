package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import arrow.core.Either
import arrow.core.toNonEmptyListOrNull
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Description
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse.DigitalObject
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse.DigitalObjectDetails
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.LinguisticObject as DomainLinguisticObject

// todo it shouldn't be here
internal val SearchUrl = UrlFrom("https://data.rijksmuseum.nl/search/collection")

internal data class PaginatedIds(
    val next: Url?,
    val ids: List<Url>,
)

internal interface SearchApi {
    suspend fun fetchArtworkIds(url: Url): Either<AppException, PaginatedIds>

    suspend fun fetchDetails(url: Url): Either<AppException, Artwork>
}

internal class SearchApiImpl(
    private val client: HttpClient,
) : SearchApi {
    override suspend fun fetchArtworkIds(url: Url): Either<AppException, PaginatedIds> =
        Either.catch {
            val response =
                client.get(url.toExternalValue()).body<HumanMadeObjectResponse.ArtworksResponse>()

            PaginatedIds(
                next = response.next?.id,
                ids = response.items.map { it.id },
            )
        }

    override suspend fun fetchDetails(url: Url): Either<AppException, Artwork> =
        Either.catch {
            val response = client.get(url.toExternalValue()).body<HumanMadeObjectResponse>()

            Artwork(
                url = url,
                title = response.title,
                primaryImage = fetchPrimaryImage(response),
                descriptions = response.toLinguisticObjects()
            )
        }

    private val HumanMadeObjectResponse.title: Title
        get() = identifiedBy
            // todo use GettyAatType to get title
            .firstOrNull { it.type == "Name" }
            ?.content
            ?.let(::Title)
            ?: error("No name found for $id")

    private suspend fun fetchPrimaryImage(response: HumanMadeObjectResponse): Url? =
        response.shows.firstNotNullOfOrNull { visualItem ->
            client.get(visualItem.id.toExternalValue())
                .body<HumanMadeObjectResponse.VisualItemDetails>()
                .digitallyShownBy
                .firstNotNullOfOrNull { digitalObjectBrief ->
                    digitalObjectDetails(digitalObjectBrief)
                }
        }?.accessPoint?.firstOrNull()?.id

    private suspend fun digitalObjectDetails(digitalObjectBrief: DigitalObject): DigitalObjectDetails =
        client.get(digitalObjectBrief.id.toExternalValue())
            .body<DigitalObject>()
            .let { digitalObject ->
                client.get(digitalObject.id.toExternalValue())
                    .body<DigitalObjectDetails>()
            }
}

private fun HumanMadeObjectResponse.toLinguisticObjects(): List<DomainLinguisticObject> =
    referredToBy
        .asSequence()
        .filter { it.language.any { lang -> lang.isDutch } }
        .mapNotNull { obj ->
            obj.content?.let { content ->
                val type = obj.classifiedAs.firstNotNullOfOrNull { GettyAatType.fromId(it.id) }

                type?.let { type -> type to Description(content) }
            }
        }
        .groupBy({ it.first }, { it.second })
        .mapNotNull { (type, descriptions) ->
            descriptions.toNonEmptyListOrNull()?.let { DomainLinguisticObject(type, it) }
        }
