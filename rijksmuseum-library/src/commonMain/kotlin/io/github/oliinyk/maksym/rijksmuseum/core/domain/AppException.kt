package io.github.oliinyk.maksym.rijksmuseum.core.domain

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Application-level exception carrying a localizable error message resource.
 *
 * Instances are created internally by the data and domain layers. Use the [displayMessage]
 * extension property in a `@Composable` context to obtain the localized string for display.
 *
 * @property resource The string resource used to resolve the human-readable error message.
 * @param cause The underlying throwable that caused this exception, if any.
 * @param message An optional non-localized message for logging/debugging purposes.
 */
public class AppException internal constructor(
    internal val resource: StringResource,
    cause: Throwable? = null,
    message: String? = null,
) : RuntimeException(message, cause)

/**
 * Returns the localized display message for this [AppException], resolved from its string resource.
 * Must be called from a `@Composable` context.
 */
public val AppException.displayMessage: String
    @Composable get() = stringResource(resource)
