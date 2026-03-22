package io.github.oliinyk.maksym.rijksmuseum.artwork.domain

import arrow.core.NonEmptyList
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.GettyAatType
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import kotlin.jvm.JvmInline

public typealias Description = NonEmptyString
public typealias Title = NonEmptyString

public data class Artwork(
    val url: Url,
    val title: Title,
    val primaryImage: Url?,
    val linguisticObjects: List<LinguisticObject>,
)

@JvmInline
public value class NonEmptyString(
    public val value: String
) : CharSequence by value {
    init {
        require(value.isNotBlank()) { "Cannot be blank" }
    }
}

public data class LinguisticObject(
    val type: GettyAatType,
    val descriptions: NonEmptyList<Description>,
)
