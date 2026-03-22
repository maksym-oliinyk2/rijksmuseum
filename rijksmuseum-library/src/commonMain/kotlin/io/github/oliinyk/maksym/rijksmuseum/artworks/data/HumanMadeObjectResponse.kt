package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import arrow.core.toNonEmptyListOrNull
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Description
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.LinguisticObject
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HumanMadeObjectResponse(
    @SerialName("id")
    @Serializable(with = UrlSerializer::class)
    val id: Url,
    @SerialName("identified_by")
    val identifiedBy: List<Identification> = emptyList(),
    @SerialName("shows")
    val shows: List<VisualItemBrief> = emptyList(),
    @SerialName("referred_to_by")
    val referredToBy: List<LinguisticObject> = emptyList(),
    @SerialName("dimension")
    val dimension: List<Dimension> = emptyList(),
) {
    @Serializable
    data class ArtworksResponse(
        @SerialName("next")
        val next: NextPage? = null,
        @SerialName("orderedItems")
        val items: List<ArtworkIdItem> = emptyList()
    )

    @Serializable
    data class NextPage(
        @SerialName("id")
        @Serializable(with = UrlSerializer::class)
        val id: Url
    )

    @Serializable
    data class ArtworkIdItem(
        @SerialName("id")
        @Serializable(with = UrlSerializer::class)
        val id: Url
    )

    @Serializable
    data class VisualItemDetails(
        @SerialName("id")
        @Serializable(with = UrlSerializer::class)
        val id: Url,
        @SerialName("digitally_shown_by")
        val digitallyShownBy: List<DigitalObject> = emptyList()
    )

    @Serializable
    data class DigitalObject(
        @SerialName("id")
        @Serializable(with = UrlSerializer::class)
        val id: Url,
    )

    @Serializable
    data class DigitalObjectDetails(
        @SerialName("access_point")
        val accessPoint: List<AccessPoint> = emptyList()
    )

    @Serializable
    data class AccessPoint(
        @SerialName("id")
        @Serializable(with = UrlSerializer::class)
        val id: Url,
    )

    @Serializable
    data class Language(
        // do not convert to Url to save some memory
        @SerialName("id")
        private val id: String,
    ) {
        internal val isDutch: Boolean
            get() = id == DutchLanguage

        private companion object {
            // well-known language ids
            const val DutchLanguage = "http://vocab.getty.edu/aat/300388256"
        }
    }

    @Serializable
    data class LinguisticObject(
        @SerialName("type")
        val type: String,
        @SerialName("content")
        val content: String? = null,
        @SerialName("classified_as")
        val classifiedAs: List<Classification> = emptyList(),
        @SerialName("language")
        val language: List<Language> = emptyList(),
    )

    @Serializable
    data class Classification(
        @SerialName("id")
        @Serializable(with = UrlSerializer::class)
        val id: Url,
        @SerialName("type")
        @Serializable(with = GettyAatTypeSerializer::class)
        val type: GettyAatType?,
    )

    @Serializable
    data class Identification(
        @SerialName("type")
        val type: String,
        @SerialName("content")
        val content: String? = null,
        @SerialName("classified_as")
        val classifiedAs: List<Classification> = emptyList(),
    )

    @Serializable
    data class Dimension(
        @SerialName("type")
        val type: String,
        @SerialName("identified_by")
        val identifiedBy: List<Identification> = emptyList(),
    )

    @Serializable
    data class VisualItemBrief(
        @SerialName("id")
        @Serializable(with = UrlSerializer::class)
        val id: Url,
    )
}

internal val HumanMadeObjectResponse.title: Title?
    get() = identifiedBy
        .asSequence()
        .filter { it.type.lowercase() == "name" }
        .minByOrNull { identification ->
            val types = identification.classifiedAs.mapNotNull { it.type }
            // try to find the most relevant title type
            when {
                GettyAatType.OriginalTitle in types -> 0
                GettyAatType.OriginalSeriesTitle in types -> 1
                else -> 2
            }
        }
        ?.content
        ?.let(Title::createOrNull)

internal val HumanMadeObjectResponse.linguisticObjects: List<LinguisticObject>
    get() = referredToBy
        .asSequence()
        .filter { it.language.any { lang -> lang.isDutch } }
        .mapNotNull { obj ->
            obj.content?.let { content ->
                val type = obj.classifiedAs.firstNotNullOfOrNull { it.type }

                type?.let { type -> type to Description(content) }
            }
        }
        .groupBy({ it.first }, { it.second })
        .mapNotNull { (type, descriptions) ->
            descriptions.toNonEmptyListOrNull()?.let { LinguisticObject(type, it) }
        }
