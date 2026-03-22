package io.github.oliinyk.maksym.rijksmuseum.core.domain

import arrow.core.NonEmptyList
import io.github.oliinyk.maksym.rijksmuseum.core.domain.NonEmptyString.Companion.createOrNull
import kotlin.jvm.JvmInline

/** A non-empty string used as a description of an artwork or linguistic object. */
public typealias Description = NonEmptyString

/** A non-empty string used as the title of an artwork. */
public typealias Title = NonEmptyString

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
 * An inline value class wrapping a [String] that is guaranteed to be non-blank.
 *
 * Use [createOrNull] to safely construct an instance, or the primary constructor when
 * the value is already known to be valid.
 */
@JvmInline
public value class NonEmptyString(
    public val value: String
) : CharSequence by value {

    public companion object {
        /** Returns a [NonEmptyString] if [value] is non-blank, or `null` otherwise. */
        public fun createOrNull(
            value: String
        ): NonEmptyString? = if (isValid(value)) NonEmptyString(value) else null

        /** Returns `true` if [s] is non-blank and therefore a valid [NonEmptyString] value. */
        public fun isValid(s: String): Boolean = s.isNotBlank()
    }

    init {
        require(isValid(value)) { "Cannot be blank" }
    }
}

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
    val descriptions: NonEmptyList<Description>,
)
