package io.github.oliinyk.maksym.rijksmuseum.search.list

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.DigitalObjectDetails
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.HumanMadeObjectResponse
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.VisualItemDetails
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.http.buildUrl
import io.ktor.http.path
import kotlin.jvm.JvmInline

// extract
public typealias AppException = Throwable

@JvmInline
public value class Title internal constructor(
    public val value: String
) {
    init {
        require(value.isNotEmpty()) { "Title cannot be empty" }
    }
}

public data class Artwork internal constructor(
    val url: Url,
    val title: Title,
    val images: List<Url>
)

internal class SearchUseCase(
    private val searchRepository: SearchRepositoryImpl
) {
    suspend fun searchArtworks(page: Url?): Either<AppException, Page<Url>> {
        return searchRepository.searchArtworks(page)
    }

    suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> {
        return searchRepository.fetchArtworkDetails(url)
    }
}

// extract

internal interface SearchRepository {
    suspend fun searchArtworks(page: Url?): Either<AppException, Page<Url>>
    suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork>
}

internal class SearchRepositoryImpl(
    // in this case hiding the API behind the interface looks like overkill to me,
    // so I'll use a pre-configured http client
    private val client: HttpClient,
) : SearchRepository {

    override suspend fun fetchArtworkDetails(url: Url): Either<AppException, Artwork> =
        client.safeRequest {
            val humanMadeObject = get(url.toExternalValue()).body<HumanMadeObjectResponse>()
            val name = humanMadeObject.identifiedBy.filter { it.type == "Name" }
                .firstNotNullOfOrNull { it.content } ?: error("name not found")
            val visualItemDetails = humanMadeObject.shows.firstNotNullOfOrNull {
                get(it.id).body<VisualItemDetails>()
            } ?: error("visualItemDetails")

            val digitalObjectDetails =
                visualItemDetails.digitallyShownBy.firstNotNullOfOrNull { get(it.id).body<DigitalObjectDetails>() }
                    ?: error("digitalObjectDetails details not found")
            val urls = digitalObjectDetails.accessPoint.map { UrlFrom(it.id) }

            Artwork(url, Title(name), urls)
        }

    override suspend fun searchArtworks(page: Url?): Either<AppException, Page<Url>> =
        client.safeRequest {
            val response = get(SearchUrl).body<SearchResponse>()

            Page(
                data = response.orderedItems.map { UrlFrom(it.id) },
                next = response.next?.id?.let { UrlFrom(it) }
            )
        }
}

/*for (item in all.orderedItems) {
                println("checking: ${item.id}")
                val humanMadeObject = get(item.id).body<HumanMadeObjectResponse>()
                val name = humanMadeObject.identifiedBy.filter { it.type == "Name" }.firstNotNullOfOrNull { it.content } ?: continue
                val visualItemDetails = humanMadeObject.shows.firstNotNullOfOrNull {
                    get(it.id).body<VisualItemDetails>()
                } ?: continue

                val digitalObjectDetails =
                    visualItemDetails.digitallyShownBy.firstNotNullOfOrNull { get(it.id).body<DigitalObjectDetails>() }
                        ?: continue
                val urls = digitalObjectDetails.accessPoint.map { it.id }

                println("FOUND URLS for ${item.id} $urls")
            }*/
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

/*
internal class RijksmuseumApiImpl(
    private val client: HttpClient,
) : RijksmuseumApi {
    override suspend fun searchArtworks(page: Url?): Either<AppException, SearchResponse> =

}
*/


internal suspend inline fun <T> HttpClient.safeRequest(
    request: suspend HttpClient.() -> T
): Either<AppException, T> = Either.catch { request() }.mapLeft { it }
