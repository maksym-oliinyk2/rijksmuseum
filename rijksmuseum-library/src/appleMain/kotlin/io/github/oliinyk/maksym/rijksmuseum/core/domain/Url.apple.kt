package io.github.oliinyk.maksym.rijksmuseum.core.domain

import platform.Foundation.NSURL

public actual typealias Url = NSURL

public actual fun UrlFrom(
    value: String
): Url = NSURL(string = value)

public actual fun Url.toStringValue(): String = toString()
