package io.github.oliinyk.maksym.rijksmuseum.core.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme.paddings

@Composable
internal fun contentPaddingValues(): PaddingValues {
    return PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding() + MaterialTheme.paddings.normal,
        start = MaterialTheme.paddings.normal,
        end = MaterialTheme.paddings.normal,
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateTopPadding() + MaterialTheme.paddings.normal,
    )
}
