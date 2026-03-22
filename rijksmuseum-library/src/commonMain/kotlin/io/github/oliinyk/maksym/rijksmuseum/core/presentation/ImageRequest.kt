package io.github.oliinyk.maksym.rijksmuseum.core.presentation

import androidx.compose.runtime.Composable
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.core.domain.toStringValue

@Composable
internal fun Url.toImageRequest(): ImageRequest = ImageRequest.Builder(LocalPlatformContext.current)
    .data(toStringValue())
    .crossfade(true)
    .build()
