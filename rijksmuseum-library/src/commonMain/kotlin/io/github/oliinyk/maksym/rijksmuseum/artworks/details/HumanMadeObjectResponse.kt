package io.github.oliinyk.maksym.rijksmuseum.artworks.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class HumanMadeObjectResponse(
    val id: String,
    @SerialName("identified_by") val identifiedBy: List<Identification> = emptyList(),
    val shows: List<VisualItemBrief> = emptyList(),
)

@Serializable
public data class Identification(
    val type: String, // filter for Name
    val content: String? = null,
)

@Serializable
public data class VisualItemBrief(
    val id: String,
)
