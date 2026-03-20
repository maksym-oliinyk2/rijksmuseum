package io.github.oliinyk.maksym.rijksmuseum.ui.common

import androidx.compose.runtime.Composable
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue

@Composable
internal fun Url.toImageRequest(): ImageRequest = ImageRequest.Builder(LocalPlatformContext.current)
    .data(toExternalValue())
    .crossfade(true)
    .build()
