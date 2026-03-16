package io.github.oliinyk.maksym.rijksmuseum.artworks.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val ItemsToShow = 10

@Composable
internal fun ArtworksScreen(
    onDetails: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
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
