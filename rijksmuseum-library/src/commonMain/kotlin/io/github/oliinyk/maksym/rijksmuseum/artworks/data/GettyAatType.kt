package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_brief_text
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_collection
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_description
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_dimensions
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_documentation
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_inscription
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_mentioned
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_notes
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_object_number
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_original_series_title
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_original_title
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_production
import io.github.oliinyk.maksym.rijksmuseum.res.getty_aat_type_work_type
import org.jetbrains.compose.resources.StringResource

/**
 * Common Getty Art & Architecture Thesaurus (AAT) types used by the Rijksmuseum API
 * to classify linguistic objects (descriptions, inscriptions, etc.).
 */
public enum class GettyAatType(
    internal val id: Url,
    public val displayName: StringResource,
) {
    /**
     * Inscriptions or marks ("Opschriften / Merken")
     */
    Inscription(UrlFrom("http://vocab.getty.edu/aat/300435414"), Res.string.getty_aat_type_inscription),

    /**
     * Descriptive notes ("Beschrijving")
     */
    Notes(UrlFrom("http://vocab.getty.edu/aat/300435452"), Res.string.getty_aat_type_notes),

    /**
     * General description ("Beschrijving" - often used with Notes)
     */
    Description(UrlFrom("http://vocab.getty.edu/aat/300404670"), Res.string.getty_aat_type_description),

    /**
     * Documentation or references ("Documentatie")
     */
    Documentation(UrlFrom("http://vocab.getty.edu/aat/300435416"), Res.string.getty_aat_type_documentation),

    /**
     * Dimensions ("Afmetingen")
     */
    Dimensions(UrlFrom("http://vocab.getty.edu/aat/300435430"), Res.string.getty_aat_type_dimensions),

    /**
     * Original title on object ("originele titel op object")
     */
    OriginalTitle(UrlFrom("https://id.rijksmuseum.nl/22015530"), Res.string.getty_aat_type_original_title),

    /**
     * Original series title ("originele serietitel")
     */
    OriginalSeriesTitle(UrlFrom("https://id.rijksmuseum.nl/22015532"), Res.string.getty_aat_type_original_series_title),

    /**
     * Collection or provenance information ("Collectie")
     */
    Collection(UrlFrom("http://vocab.getty.edu/aat/300026687"), Res.string.getty_aat_type_collection),

    /**
     * Brief text classification
     */
    BriefText(UrlFrom("http://vocab.getty.edu/aat/300418049"), Res.string.getty_aat_type_brief_text),

    /**
     * Type of Work classification
     */
    WorkType(UrlFrom("http://vocab.getty.edu/aat/300435443"), Res.string.getty_aat_type_work_type),

    /**
     * Object number classification
     */
    ObjectNumber(UrlFrom("http://vocab.getty.edu/aat/300312355"), Res.string.getty_aat_type_object_number),

    /**
     * Production classification
     */
    Production(UrlFrom("http://vocab.getty.edu/aat/300404450"), Res.string.getty_aat_type_production),

    /**
     * Mentioned in documentation or reference
     */
    Mentioned(UrlFrom("http://vocab.getty.edu/aat/300028705"), Res.string.getty_aat_type_mentioned);

    internal companion object {
        fun fromId(id: Url): GettyAatType? = entries.firstOrNull { it.id == id }
    }
}
