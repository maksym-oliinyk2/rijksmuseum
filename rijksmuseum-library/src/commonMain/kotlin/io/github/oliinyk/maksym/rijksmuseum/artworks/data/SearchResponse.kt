package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import kotlinx.serialization.Serializable

@Serializable
public data class SearchResponse(
    val next: OrderedCollectionPage? = null,
    val orderedItems: List<Item>
)

@Serializable
public data class OrderedCollectionPage(
    val id: String
)

@Serializable
public data class Item(
    val id: String
)
