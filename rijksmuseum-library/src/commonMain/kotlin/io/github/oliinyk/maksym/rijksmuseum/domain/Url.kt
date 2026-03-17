package io.github.oliinyk.maksym.rijksmuseum.domain

public expect class Url

@Suppress("FunctionName")
public expect fun UrlFrom(
    value: String
): Url

public expect fun Url.toExternalValue(): String
