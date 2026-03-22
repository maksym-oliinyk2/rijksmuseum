package io.github.oliinyk.maksym.rijksmuseum.core.domain

import arrow.core.NonEmptyList
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

    public companion object {
        public fun createOrNull(
            value: String
        ): NonEmptyString? = if (isValid(value)) NonEmptyString(value) else null

        public fun isValid(s: String): Boolean = s.isNotBlank()
    }

    init {
        require(isValid(value)) { "Cannot be blank" }
    }
}

public data class LinguisticObject(
    val type: GettyAatType,
    val descriptions: NonEmptyList<Description>,
)
