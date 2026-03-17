package io.github.oliinyk.maksym.rijksmuseum.artworks.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class VisualItemDetails(
    @SerialName("@context")
    val context: String,
    val id: String,
    val type: String,// expected VisualItem
    @SerialName("digitally_shown_by")
    val digitallyShownBy: List<DigitalObject> = emptyList()
)

@Serializable
public data class DigitalObject(
    val id: String,
    val type: String,//expected DigitalObject
)

@Serializable
public data class DigitalObjectDetails(
    val id: String,
    val type: String,//expected DigitalObject
    @SerialName("access_point")
    val accessPoint: List<AccessPoint> = emptyList()
)

@Serializable
public data class AccessPoint(
    val id: String,
    val type: String,//expected DigitalObject
)
