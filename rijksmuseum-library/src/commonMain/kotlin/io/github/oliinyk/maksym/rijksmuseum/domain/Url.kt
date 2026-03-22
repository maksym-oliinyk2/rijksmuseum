package io.github.oliinyk.maksym.rijksmuseum.domain

public expect class Url

public expect fun UrlFrom(
    value: String
): Url

public expect fun Url.toStringValue(): String
