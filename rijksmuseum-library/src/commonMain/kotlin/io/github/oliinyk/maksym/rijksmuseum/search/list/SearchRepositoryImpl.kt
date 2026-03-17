package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.DigitalObjectDetails
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.VisualItemDetails
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.http.buildUrl
import io.ktor.http.path

public typealias AppException = Throwable

internal class SearchUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend fun searchArtworks(): Either<AppException, SearchResponse> =
        searchRepository.searchArtworks()
}

internal interface SearchRepository {
    // todo use domain model
    suspend fun searchArtworks(): Either<AppException, SearchResponse>
}

internal class SearchRepositoryImpl(
    private val api: RijksmuseumApi
) : SearchRepository {
    // todo use domain model
    override suspend fun searchArtworks(): Either<AppException, SearchResponse> =
        api.searchArtworks()

}

internal interface RijksmuseumApi {

    suspend fun searchArtworks(): Either<AppException, SearchResponse>

}

private val SearchUrl = buildUrl {
    protocol = HTTPS
    host = "data.rijksmuseum.nl"
    path("search", "collection")
}

private fun DetailsUrl(id: String) = buildUrl {
    protocol = HTTPS
    host = "id.rijksmuseum.nl"
    path(id)
}

internal class RijksmuseumApiImpl(
    private val client: HttpClient,
) : RijksmuseumApi {
    override suspend fun searchArtworks(): Either<AppException, SearchResponse> =
        client.runRequest {
            val all = get(SearchUrl).body<SearchResponse>()

            for (item in all.orderedItems) {
                println("checking: ${item.id}")
                val humanMadeObject = get(item.id).body<HumanMadeObjectResponse>()
                val visualItemDetails = humanMadeObject.shows.firstNotNullOfOrNull {
                    get(it.id).body<VisualItemDetails>()
                } ?: continue

                val digitalObjectDetails =
                    visualItemDetails.digitallyShownBy.firstNotNullOfOrNull { get(it.id).body<DigitalObjectDetails>() }
                        ?: continue
                val urls = digitalObjectDetails.accessPoint.map { it.id }

                println("FOUND URLS for ${item.id} $urls")
            }

            all
        }

}


internal suspend inline fun <T> HttpClient.runRequest(
    request: suspend HttpClient.() -> T
): Either<AppException, T> = Either.catch { request() }.mapLeft { it }
