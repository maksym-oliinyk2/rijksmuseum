package io.github.oliinyk.maksym.rijksmuseum.core.data

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.HumanMadeObjectResponse.ArtworksResponse
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.HumanMadeObjectResponse.DigitalObject
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.HumanMadeObjectResponse.DigitalObjectDetails
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.HumanMadeObjectResponse.VisualItemDetails
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.linguisticObjects
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.title
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.core.domain.toStringValue
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.exception_unknown
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RijksmuseumApiImpl(
    private val client: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : RijksmuseumApi {
    override suspend fun fetchArtworkIds(page: Url): Either<AppException, PaginatedIds> =
        withContext(dispatcher) {
            Either.catch {
                val response = client.fetch<ArtworksResponse>(page)

                PaginatedIds(
                    next = response.next?.id,
                    ids = response.items.map { it.id },
                )
            }.mapLeft { AppException(Res.string.exception_unknown, it) }
        }

    override suspend fun fetchArtwork(url: Url): Either<AppException, Artwork> =
        withContext(dispatcher) {
            Either.catch {
                val response = client.fetch<HumanMadeObjectResponse>(url)

                Artwork(
                    url = url,
                    title = response.title ?: error("No name found for $url"),
                    primaryImage = fetchPrimaryImage(response),
                    linguisticObjects = response.linguisticObjects,
                )
            }.mapLeft { AppException(Res.string.exception_unknown, it) }
        }

    private suspend fun fetchPrimaryImage(response: HumanMadeObjectResponse): Url? {
        val digitalObjects = response.shows
            .firstNotNullOfOrNull { client.fetch<VisualItemDetails>(it.id).digitallyShownBy }

        val digitalObjectDetails = digitalObjects?.firstNotNullOfOrNull { digitalObjectDetails(it) }

        return digitalObjectDetails?.accessPoint?.firstOrNull()?.id
    }

    private suspend fun digitalObjectDetails(digitalObjectBrief: DigitalObject): DigitalObjectDetails {
        val digitalObject = client.fetch<DigitalObject>(digitalObjectBrief.id)

        return client.fetch<DigitalObjectDetails>(digitalObject.id)
    }
}

private suspend inline fun <reified T> HttpClient.fetch(
    url: Url
): T = get(url.toStringValue()).body<T>()
