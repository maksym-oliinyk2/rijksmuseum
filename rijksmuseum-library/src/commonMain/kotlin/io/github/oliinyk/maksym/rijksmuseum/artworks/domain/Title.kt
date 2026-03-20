package io.github.oliinyk.maksym.rijksmuseum.artworks.domain

import kotlin.jvm.JvmInline

/**
 * Represents the title of an artwork.
 */
@JvmInline
public value class Title(
    public val value: String
) {
    init {
        require(value.isNotBlank()) { "Title cannot be blank" }
    }
}
