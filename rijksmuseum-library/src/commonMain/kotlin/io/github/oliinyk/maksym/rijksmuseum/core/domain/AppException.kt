package io.github.oliinyk.maksym.rijksmuseum.core.domain

import androidx.compose.runtime.Composable
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.exception_unknown
import org.jetbrains.compose.resources.stringResource

/**
 * Exception type used across the search domain to represent application-level errors.
 */
public typealias AppException = Throwable

public val AppException.displayMessage: String
    @Composable get() = message ?: stringResource(Res.string.exception_unknown)
