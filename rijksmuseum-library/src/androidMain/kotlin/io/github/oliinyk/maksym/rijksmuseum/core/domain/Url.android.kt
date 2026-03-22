package io.github.oliinyk.maksym.rijksmuseum.core.domain

public actual typealias Url = java.net.URI

public actual fun UrlFrom(
    value: String
): Url = java.net.URI(value)

public actual fun Url.toStringValue(): String = toASCIIString()
