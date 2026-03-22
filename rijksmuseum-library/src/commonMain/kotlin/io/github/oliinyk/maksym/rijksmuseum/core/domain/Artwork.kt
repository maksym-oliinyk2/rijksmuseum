package io.github.oliinyk.maksym.rijksmuseum.core.domain

/** A non-empty string used as a description of an artwork or linguistic object. */
public typealias Description = String

/** A non-empty string used as the title of an artwork. */
public typealias Title = String

/**
 * Domain model representing a single artwork from the Rijksmuseum collection.
 *
 * @property url The canonical URL identifying this artwork in the Rijksmuseum Linked Data API.
 * @property title The primary title of the artwork. Always non-empty.
 * @property primaryImage URL of the primary image for this artwork, or `null` if no image is available.
 * @property linguisticObjects A list of textual descriptions, inscriptions, and other linguistic metadata
 *   associated with the artwork, each classified by a [GettyAatType].
 */
public data class Artwork(
    val url: Url,
    val title: Title,
    val primaryImage: Url?,
    val linguisticObjects: List<LinguisticObject>,
)

/**
 * A textual object associated with an artwork, classified by a Getty AAT [type].
 *
 * Examples include descriptions, inscriptions, dimensions, and provenance notes.
 *
 * @property type The Getty AAT classification that describes the role of this linguistic object.
 * @property descriptions One or more non-empty text values for this linguistic object.
 */
public data class LinguisticObject(
    val type: GettyAatType,
    val descriptions: List<Description>,
)
