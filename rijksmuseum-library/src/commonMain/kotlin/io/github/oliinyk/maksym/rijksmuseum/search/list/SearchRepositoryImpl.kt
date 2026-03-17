package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
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

private val DetailsUrl = buildUrl {
    protocol = HTTPS
    host = "data.rijksmuseum.nl"
    path("search", "collection")
}

internal class RijksmuseumApiImpl(
    private val client: HttpClient,
) : RijksmuseumApi {
    override suspend fun searchArtworks(): Either<AppException, SearchResponse> =
        client.runRequest {
            get(SearchUrl).body()
        }

}


internal suspend inline fun <T> HttpClient.runRequest(
    request: suspend HttpClient.() -> T
): Either<AppException, T> = Either.catch { request() }.mapLeft { it }
