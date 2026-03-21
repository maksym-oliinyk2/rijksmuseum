package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import arrow.core.Either
import arrow.core.toNonEmptyListOrNull
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Description
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse.ArtworksResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse.DigitalObject
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse.DigitalObjectDetails
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.HumanMadeObjectResponse.VisualItemDetails
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.toStringValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.LinguisticObject as DomainLinguisticObject

internal data class PaginatedIds(
    val next: Url?,
    val ids: List<Url>,
)

internal interface RijksmuseumApi {

    suspend fun fetchArtworkIds(page: Url): Either<AppException, PaginatedIds>

    suspend fun fetchArtwork(url: Url): Either<AppException, Artwork>
}

internal class RijksmuseumApiImpl(
    private val client: HttpClient,
) : RijksmuseumApi {
    override suspend fun fetchArtworkIds(page: Url): Either<AppException, PaginatedIds> =
        Either.catch {
            val response = client.fetch<ArtworksResponse>(page)

            PaginatedIds(
                next = response.next?.id,
                ids = response.items.map { it.id },
            )
        }

    override suspend fun fetchArtwork(url: Url): Either<AppException, Artwork> =
        Either.catch {
            val response = client.fetch<HumanMadeObjectResponse>(url)

            Artwork(
                url = url,
                title = response.title,
                primaryImage = fetchPrimaryImage(response),
                descriptions = response.toLinguisticObjects()
            )
        }

    private val HumanMadeObjectResponse.title: Title
        get() = identifiedBy
            .asSequence()
            .filter { it.type.lowercase() == "name" }
            .minByOrNull { identification ->
                val types = identification.classifiedAs.mapNotNull { GettyAatType.fromId(it.id) }
                when {
                    GettyAatType.OriginalTitle in types -> 0
                    GettyAatType.OriginalSeriesTitle in types -> 1
                    else -> 2
                }
            }
            ?.content
            ?.let(::Title)
            ?: error("No name found for $id")

    private suspend fun fetchPrimaryImage(response: HumanMadeObjectResponse): Url? =
        response.shows.firstNotNullOfOrNull { visualItem ->
            client.fetch<VisualItemDetails>(visualItem.id)
                .digitallyShownBy
                .firstNotNullOfOrNull { digitalObjectBrief ->
                    digitalObjectDetails(digitalObjectBrief)
                }
        }?.accessPoint?.firstOrNull()?.id

    private suspend fun digitalObjectDetails(digitalObjectBrief: DigitalObject): DigitalObjectDetails =
        client.fetch<DigitalObject>(digitalObjectBrief.id)
            .let { digitalObject ->
                client.fetch<DigitalObjectDetails>(digitalObject.id)
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

private suspend inline fun <reified T> HttpClient.fetch(
    url: Url
): T = get(url.toStringValue()).body<T>()
