package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.displayMessage
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksError
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ProgressIndicator
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.toImageRequest
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshing
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.paddings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
internal fun ArtworkDetailsScreen(
    viewModel: ArtworkDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val messageHandle = remember { MutableSharedFlow<Message>() }
    val state by viewModel(messageHandle).collectAsStateWithLifecycle(null)
    val currentState = state

    if (currentState != null) {
        val scope = rememberCoroutineScope()

        ArtworkDetailsContent(
            modifier = modifier,
            state = currentState,
            onRefresh = { scope.launch { messageHandle.emit(Message.OnRefresh) } },
            onReload = { scope.launch { messageHandle.emit(Message.OnReload) } },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
internal fun ArtworkDetailsContent(
    state: ArtworkDetailsViewState,
    onRefresh: () -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = rememberPullRefreshState(
        refreshing = state.artwork.isRefreshing,
        onRefresh = onRefresh,
    )

    Scaffold(
        modifier = modifier.navigationBarsPadding(),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .pullRefresh(refreshState, state.artwork.isRefreshable),
            contentAlignment = Alignment.TopCenter
        ) {
            ArtworkLoadableContent(
                state = state.artwork,
                onRefresh = onRefresh,
                onReload = onReload,
            )

            PullRefreshIndicator(
                modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
                refreshing = state.artwork.isRefreshing,
                state = refreshState,
            )
        }
    }
}

@Composable
private fun ArtworkLoadableContent(
    state: Loadable<Artwork?>,
    onRefresh: () -> Unit,
    onReload: () -> Unit,
) {
    when (val s = state.state) {
        is Loadable.Exception -> ArtworksError(
            modifier = Modifier.fillMaxSize(),
            message = s.exception.displayMessage,
            onRetry = onReload
        )

        Loadable.Loading -> ProgressIndicator(modifier = Modifier.fillMaxSize())

        Loadable.Idle, Loadable.Refreshing -> {
            val artwork = state.data
            if (artwork != null) {
                ArtworkDetails(artwork)
            } else {
                // Handle empty data if necessary, though for details it's likely an error if idle and null
                ArtworksError(
                    modifier = Modifier.fillMaxSize(),
                    message = "No data available",
                    onRetry = onRefresh
                )
            }
        }

        else -> {}
    }
}

@Composable
private fun ArtworkDetails(
    artwork: Artwork,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.paddings.normal),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.normal)
    ) {
        item {
            Text(
                text = artwork.title.value,
                style = MaterialTheme.typography.h4
            )
        }

        items(artwork.images) { imageUrl ->
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                model = imageUrl.toImageRequest(),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }

        items(artwork.descriptions) { linguisticObject ->
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)) {
                Text(
                    text = linguisticObject.type.name,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = linguisticObject.description.value,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}
