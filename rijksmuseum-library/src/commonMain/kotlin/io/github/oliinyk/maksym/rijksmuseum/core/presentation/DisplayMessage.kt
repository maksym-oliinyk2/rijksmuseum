package io.github.oliinyk.maksym.rijksmuseum.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme.RijksmuseumTheme
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme.paddings
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_action_retry
import org.jetbrains.compose.resources.stringResource

internal const val DisplayMessageTag: String = "Display message"

@Composable
internal fun DisplayMessage(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit,
    imageVector: ImageVector? = null,
) {
    Column(
        modifier = modifier
            .semantics(true) { testTag = DisplayMessageTag }
            .padding(MaterialTheme.paddings.normal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.normal),
    ) {
        if (imageVector != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = null
            )
        }

        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = typography.body1
        )

        TextButton(onClick = onRetry) {
            Text(stringResource(Res.string.artworks_action_retry))
        }
    }
}

@Composable
@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
private fun DisplayMessagePreview() {
    RijksmuseumTheme {
        DisplayMessage(
            modifier = Modifier,
            message = "Something went wrong. Please try again later.",
            onRetry = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
private fun DisplayMessageWithIconPreview() {
    RijksmuseumTheme {
        DisplayMessage(
            modifier = Modifier,
            imageVector = Icons.Default.Error,
            message = "Something went wrong. Please try again later.",
            onRetry = {}
        )
    }
}
