package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

private const val ItemsToShow = 10

@Composable
internal fun ArtworksScreen(
    onDetails: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtworksViewModel = koinViewModel<ArtworksViewModel>()
) {
    LazyColumn(modifier = modifier) {
        items(ItemsToShow) { i ->
            Button(onClick = {
                onDetails(i)
            }) {
                Text("$i")
            }
        }
    }
}
