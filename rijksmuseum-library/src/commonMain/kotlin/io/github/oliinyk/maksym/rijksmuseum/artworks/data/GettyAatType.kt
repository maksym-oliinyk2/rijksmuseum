package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom

/**
 * Common Getty Art & Architecture Thesaurus (AAT) types used by the Rijksmuseum API
 * to classify linguistic objects (descriptions, inscriptions, etc.).
 */
public enum class GettyAatType(
    internal val id: Url,
) {
    /**
     * Inscriptions or marks ("Opschriften / Merken")
     */
    Inscription(UrlFrom("http://vocab.getty.edu/aat/300435414")),

    /**
     * Descriptive notes ("Beschrijving")
     */
    Notes(UrlFrom("http://vocab.getty.edu/aat/300435452")),

    /**
     * General description ("Beschrijving" - often used with Notes)
     */
    Description(UrlFrom("http://vocab.getty.edu/aat/300404670")),

    /**
     * Documentation or references ("Documentatie")
     */
    Documentation(UrlFrom("http://vocab.getty.edu/aat/300435416")),

    /**
     * Dimensions ("Afmetingen")
     */
    Dimensions(UrlFrom("http://vocab.getty.edu/aat/300435430")),

    /**
     * Original title on object ("originele titel op object")
     */
    OriginalTitle(UrlFrom("https://id.rijksmuseum.nl/22015530")),

    /**
     * Original series title ("originele serietitel")
     */
    OriginalSeriesTitle(UrlFrom("https://id.rijksmuseum.nl/22015532")),

    /**
     * Collection or provenance information ("Collectie")
     */
    Collection(UrlFrom("http://vocab.getty.edu/aat/300026687")),

    /**
     * Brief text classification
     */
    BriefText(UrlFrom("http://vocab.getty.edu/aat/300418049")),

    /**
     * Type of Work classification
     */
    WorkType(UrlFrom("http://vocab.getty.edu/aat/300435443")),

    /**
     * Object number classification
     */
    ObjectNumber(UrlFrom("http://vocab.getty.edu/aat/300312355")),

    /**
     * Production classification
     */
    Production(UrlFrom("http://vocab.getty.edu/aat/300404450")),

    /**
     * Mentioned in documentation or reference
     */
    Mentioned(UrlFrom("http://vocab.getty.edu/aat/300028705"));

    internal companion object {
        fun fromId(id: Url): GettyAatType? = entries.firstOrNull { it.id == id }
    }
}
