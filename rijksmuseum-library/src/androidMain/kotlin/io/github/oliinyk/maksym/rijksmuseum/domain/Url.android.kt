package io.github.oliinyk.maksym.rijksmuseum.domain

public actual typealias Url = java.net.URI

@Suppress("FunctionName")
public actual fun UrlFrom(
    value: String
): Url = java.net.URI(value)

public actual fun Url.toExternalValue(): String = toASCIIString()
