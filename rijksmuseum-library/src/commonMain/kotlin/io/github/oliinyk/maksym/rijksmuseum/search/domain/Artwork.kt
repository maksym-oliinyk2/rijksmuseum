package io.github.oliinyk.maksym.rijksmuseum.search.domain

import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import kotlin.jvm.JvmInline

/**
 * Exception type used across the search domain to represent application-level errors.
 */
public typealias AppException = Throwable

/**
 * Represents the title of an artwork.
 */
@JvmInline
public value class Title internal constructor(
    public val value: String
) {
    init {
        require(value.isNotEmpty()) { "Title cannot be empty" }
    }
}

/**
 * Represents an artwork with its source URL, title, and a list of image URLs.
 */
public data class Artwork internal constructor(
    val url: Url,
    val title: Title,
    val images: List<Url>
)
