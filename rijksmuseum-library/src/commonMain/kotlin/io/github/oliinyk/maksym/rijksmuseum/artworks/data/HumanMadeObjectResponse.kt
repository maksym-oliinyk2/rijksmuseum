package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HumanMadeObjectResponse(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url,
    @SerialName("identified_by") val identifiedBy: List<Identification> = emptyList(),
    @SerialName("shows")
    val shows: List<VisualItemBrief> = emptyList(),
)

@Serializable
internal data class Identification(
    @SerialName("type")
    val type: String, // filter for Name
    @SerialName("content")
    val content: String? = null,
)

@Serializable
internal data class VisualItemBrief(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url,
)
