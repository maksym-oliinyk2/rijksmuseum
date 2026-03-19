package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import kotlinx.serialization.Serializable

@Serializable
internal data class ArtworksResponse(
    val next: NextPage? = null,
    val items: List<ArtworkIdItem>
)

@Serializable
internal data class NextPage(
    val id: String
)

@Serializable
internal data class ArtworkIdItem(
    val id: String
)
