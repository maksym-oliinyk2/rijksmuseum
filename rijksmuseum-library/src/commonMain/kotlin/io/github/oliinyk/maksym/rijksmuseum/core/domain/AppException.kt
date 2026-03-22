package io.github.oliinyk.maksym.rijksmuseum.core.domain

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Exception type used across the search domain to represent application-level errors.
 */
public class AppException internal constructor(
    internal val resource: StringResource,
    cause: Throwable? = null,
    message: String? = null,
) : RuntimeException(message, cause)

public val AppException.displayMessage: String
    @Composable get() = stringResource(resource)
