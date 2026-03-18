package io.github.oliinyk.maksym.rijksmuseum.artworks.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class VisualItemDetails(
    val id: String,
    @SerialName("digitally_shown_by")
    val digitallyShownBy: List<DigitalObject> = emptyList()
)

@Serializable
public data class DigitalObject(
    val id: String,
)

@Serializable
public data class DigitalObjectDetails(
    @SerialName("access_point")
    val accessPoint: List<AccessPoint> = emptyList()
)

@Serializable
public data class AccessPoint(
    val id: String,
)
