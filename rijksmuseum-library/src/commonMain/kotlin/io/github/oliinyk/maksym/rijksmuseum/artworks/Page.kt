package io.github.oliinyk.maksym.rijksmuseum.artworks

import io.github.oliinyk.maksym.rijksmuseum.domain.Url

public data class Page<out T>(
    val data: List<T>,
    val next: Url? = null,
)

public inline val Page<*>.hasMore: Boolean
    get() = next != null

