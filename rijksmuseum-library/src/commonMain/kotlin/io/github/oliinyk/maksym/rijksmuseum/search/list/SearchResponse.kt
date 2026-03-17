package io.github.oliinyk.maksym.rijksmuseum.search.list

import kotlinx.serialization.Serializable

@Serializable
public data class SearchResponse(
    //@SerialName("@context")
    // val context: String,
    val id: String,
    //val type: String,
    val partOf: Collection,//todo do we need it?
    val next: OrderedCollectionPage? = null,
    val prev: OrderedCollectionPage? = null,
    val startIndex: Int? = null,
    val orderedItems: List<Item>
)

@Serializable
public data class Collection(
    val id: String,
    val type: String,
    val totalItems: Int? = null,
    val first: OrderedCollectionPage? = null,
    val last: OrderedCollectionPage? = null
)

@Serializable
public data class OrderedCollectionPage(
    val id: String,
    val type: String
)

@Serializable
public data class Item(
    val id: String,
    val type: String
)
