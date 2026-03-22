package io.github.oliinyk.maksym.rijksmuseum.core.domain

/**
 * Platform-specific representation of a URL.
 *
 * On Android this maps to `android.net.Uri`; on Apple platforms it maps to `Foundation.URL`.
 * Use [UrlFrom] to construct an instance and [Url.toStringValue] to retrieve the string form.
 */
public expect class Url

/** Creates a [Url] from the given [value] string. Throws if [value] is not a valid URL on the target platform. */
public expect fun UrlFrom(
    value: String
): Url

/** Returns the string representation of this [Url]. */
public expect fun Url.toStringValue(): String
