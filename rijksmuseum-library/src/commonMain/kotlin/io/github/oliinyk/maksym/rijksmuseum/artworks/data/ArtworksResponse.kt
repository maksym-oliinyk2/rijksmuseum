package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ArtworksResponse(
    @SerialName("next")
    val next: NextPage? = null,
    @SerialName("orderedItems")
    val items: List<ArtworkIdItem> = emptyList()
)

@Serializable
internal data class NextPage(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url
)

@Serializable
internal data class ArtworkIdItem(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url
)
