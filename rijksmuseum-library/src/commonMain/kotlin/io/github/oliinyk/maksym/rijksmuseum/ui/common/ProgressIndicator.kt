package io.github.oliinyk.maksym.rijksmuseum.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

public const val ProgressIndicatorTag: String = "Progress indicator"

@Composable
internal fun ProgressIndicator(
    modifier: Modifier,
) {
    Box(
        modifier = modifier.testTag(ProgressIndicatorTag),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
