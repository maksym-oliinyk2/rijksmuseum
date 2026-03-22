package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal typealias MessageHandler<M> = (M) -> Unit

@Composable
internal fun <M> rememberMessageHandler(
    input: suspend (M) -> Unit,
): MessageHandler<M> {
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }

    return remember(scope, input) {
        {
            scope.launch { input(it) }
        }
    }
}
