package io.github.oliinyk.maksym.rijksmuseum.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_action_retry
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.paddings
import org.jetbrains.compose.resources.stringResource

internal const val DisplayMessageTag: String = "Display message"

@Composable
internal fun DisplayMessage(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier
            .semantics(true) { testTag = DisplayMessageTag }
            .padding(MaterialTheme.paddings.normal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = typography.body1
        )
        Spacer(modifier = Modifier.height(MaterialTheme.paddings.normal))
        Button(onClick = onRetry) {
            Text(stringResource(Res.string.artworks_action_retry))
        }
    }
}
