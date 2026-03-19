package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class VisualItemDetails(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url,
    @SerialName("digitally_shown_by")
    val digitallyShownBy: List<DigitalObject> = emptyList()
)

@Serializable
public data class DigitalObject(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url,
)

@Serializable
public data class DigitalObjectDetails(
    @SerialName("access_point")
    val accessPoint: List<AccessPoint> = emptyList()
)

@Serializable
public data class AccessPoint(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url,
)
