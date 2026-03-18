package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ArtworksScreen(
    modifier: Modifier = Modifier,
    viewModel: ArtworksViewModel = koinViewModel<ArtworksViewModel>()
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val component = remember { viewModel.component(messages) }
    val state by component.collectAsState(null)
    val currentState = state

    if (currentState != null) {
        LazyColumn(modifier = modifier) {
            items(
                items = currentState.artworks.data,
                key = { it.url.toExternalValue() }
            ) { i ->
                Button(onClick = {
                    // onDetails(i)
                }) {
                    Text(i.title.value)
                }
            }
        }
    }
}
