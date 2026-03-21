package io.github.oliinyk.maksym.rijksmuseum.domain

import platform.Foundation.NSURL

public actual typealias Url = NSURL

@Suppress("FunctionName")
public actual fun UrlFrom(
    value: String
): Url = NSURL(string = value)

public actual fun Url.toStringValue(): String = toString()
