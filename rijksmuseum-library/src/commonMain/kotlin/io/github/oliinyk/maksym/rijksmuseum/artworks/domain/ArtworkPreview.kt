package io.github.oliinyk.maksym.rijksmuseum.artworks.domain

import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import kotlin.jvm.JvmInline

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
public data class ArtworkPreview internal constructor(
    val url: Url,
    val title: Title,
    val images: List<Url>
)
