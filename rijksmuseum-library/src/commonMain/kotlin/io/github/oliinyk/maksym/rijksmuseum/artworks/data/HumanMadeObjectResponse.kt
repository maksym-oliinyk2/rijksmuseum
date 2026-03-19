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
    @SerialName("referred_to_by")
    val referredToBy: List<LinguisticObject> = emptyList(),
    @SerialName("dimension")
    val dimension: List<Dimension> = emptyList(),
)

internal fun HumanMadeObjectResponse.categorizedReferredToBy(): List<Pair<GettyAatType, LinguisticObject>> =
    referredToBy.flatMap { obj ->
        obj.classifiedAs.mapNotNull { GettyAatType.fromId(it.id) }.map { it to obj }
    }

@Serializable
internal data class LinguisticObject(
    @SerialName("type")
    val type: String,
    @SerialName("content")
    val content: String? = null,
    @SerialName("classified_as")
    val classifiedAs: List<Classification> = emptyList(),
)

@Serializable
internal data class Classification(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url,
    @SerialName("type")
    val type: String,
)

@Serializable
internal data class Identification(
    @SerialName("type")
    val type: String, // filter for Name
    @SerialName("content")
    val content: String? = null,
    @SerialName("classified_as")
    val classifiedAs: List<Classification> = emptyList(),
)

@Serializable
internal data class Dimension(
    @SerialName("type")
    val type: String,
    @SerialName("identified_by")
    val identifiedBy: List<Identification> = emptyList(),
)

@Serializable
internal data class VisualItemBrief(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url,
)
