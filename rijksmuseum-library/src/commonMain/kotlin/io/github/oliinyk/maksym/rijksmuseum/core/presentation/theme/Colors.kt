package io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

internal val DarkColorScheme = darkColors(
    primary = Color.White,
    secondary = Color(4, 4, 6),
    secondaryVariant = Color.White,
    background = Color.Black,
    surface = Color(4, 4, 6),
    error = Color(0xFFF70040),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

internal val LightColorScheme = lightColors(
    primary = Color.Black,
    onPrimary = Color.Black,
    secondary = Color(18, 18, 18),
    secondaryVariant = Color(18, 18, 18),
    onSecondary = Color.Black,
    background = Color(242, 242, 242),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFD00036),
    onError = Color.White
)
