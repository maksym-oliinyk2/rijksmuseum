package io.github.oliinyk.maksym.rijksmuseum.artwork.domain

import arrow.core.NonEmptyList
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.GettyAatType
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import kotlin.jvm.JvmInline

public data class Artwork(
    val url: Url,
    val title: Title,
    val primaryImage: Url?,
    val descriptions: List<LinguisticObject>,
)

public data class LinguisticObject(
    val type: GettyAatType,
    val descriptions: NonEmptyList<Description>,
)

@JvmInline
public value class Description(
    public val value: String
) {
    init {
        require(value.isNotBlank()) { "Description cannot be blank" }
    }
}
