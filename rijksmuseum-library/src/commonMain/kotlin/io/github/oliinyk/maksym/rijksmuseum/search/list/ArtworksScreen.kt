package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ArtworksScreen(
    onDetails: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtworksViewModel = koinViewModel<ArtworksViewModel>()
) {

    val state by viewModel.state.collectAsState()

    LazyColumn(modifier = modifier) {

        items(
            items = state.artworks.data,
            key = { it.url.toExternalValue() }
        ) { i ->
            Button(onClick = {
                //onDetails(i)
            }) {
                Text(i.title.value)
            }
        }
    }
}
