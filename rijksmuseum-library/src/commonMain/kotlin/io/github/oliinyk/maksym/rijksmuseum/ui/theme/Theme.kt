package io.github.oliinyk.maksym.rijksmuseum.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
public fun RijksmuseumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPaddings provides Paddings(),
    ) {
        MaterialTheme(
            colors = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = typography(),
            content = content
        )
    }
}

@Suppress("UnusedReceiverParameter")
public val MaterialTheme.paddings: Paddings
    @Composable
    @ReadOnlyComposable
    get() = LocalPaddings.current
