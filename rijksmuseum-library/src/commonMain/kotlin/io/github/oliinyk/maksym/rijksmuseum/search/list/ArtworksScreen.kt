package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.github.oliinyk.maksym.rijksmuseum.search.domain.Artwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ArtworksScreen(
    onNavigateToDetails: (Artwork) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtworksViewModel = koinViewModel<ArtworksViewModel>()
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val component = remember { viewModel.component(messages) }
    val state by component.collectAsStateWithLifecycle(null)
    val currentState = state

    if (currentState != null) {
        val scope = rememberCoroutineScope { Dispatchers.Main.immediate }

        ArtworksContent(
            modifier = modifier,
            state = currentState,
            onMessage = { scope.launch { messages.emit(it) } },
            onNavigateToDetails = onNavigateToDetails
        )
    }
}


@Composable
private fun ArtworksContent(
    state: ViewState,
    onMessage: (Message) -> Unit,
    onNavigateToDetails: (Artwork) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.navigationBarsPadding(),
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues),
            contentPadding = contentPaddingValues(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.artworks.data.isNotEmpty()) {
                artworkItems(state.artworks, onNavigateToDetails)
            }

            paginateableContent(
                paginateable = state.artworks,
                onMessage = onMessage
            )

            item {
                LaunchedEffect(Unit) {
                    onMessage(Message.OnLoadNext)
                }
            }
        }
    }
}

private fun LazyListScope.artworkItems(
    paginateable: Paginateable<Artwork>,
    onNavigateToDetails: (Artwork) -> Unit,
) {
    items(
        items = paginateable.data,
        key = { item -> item.url.toExternalValue() },
    ) { artwork ->
        ArtworkItem(
            artwork = artwork,
            onClick = { onNavigateToDetails(artwork) }
        )
    }
}

private fun LazyListScope.paginateableContent(
    paginateable: Paginateable<Artwork>,
    onMessage: (Message) -> Unit,
) = item(
    key = paginateable.state::class.simpleName,
    contentType = paginateable.state::class
) {
    when (val state = paginateable.state) {
        is Paginateable.Exception ->
            ArtworksError(
                modifier = if (paginateable.data.isEmpty()) Modifier.fillParentMaxSize() else Modifier.fillParentMaxWidth(),
                message = state.exception.message ?: "Unknown error",
                onRetry = { onMessage(if (paginateable.data.isEmpty()) Message.OnRefresh else Message.OnLoadNext) }
            )

        is Paginateable.Loading -> ArtworksProgress(modifier = Modifier.fillParentMaxSize())
        is Paginateable.LoadingNext -> ArtworksProgress(modifier = Modifier.fillParentMaxWidth())
        is Paginateable.Idle, is Paginateable.Refreshing -> {
            if (paginateable.data.isEmpty()) {
                ArtworksError(
                    modifier = Modifier.fillParentMaxSize(),
                    message = "No artworks found",
                    onRetry = { onMessage(Message.OnRefresh) }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ArtworkItem(
    artwork: Artwork,
    onClick: () -> Unit,
) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Column {
            ArtworkImage(imageUrl = artwork.images.firstOrNull())
            Spacer(modifier = Modifier.height(8.dp))
            ArtworkContents(artwork = artwork)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ArtworkImage(
    imageUrl: Url?,
) {
    Surface(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        color = colors.onSurface.copy(alpha = 0.2f)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl.toImageRequest(),
                contentDescription = "Artwork Image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun ArtworkContents(
    artwork: Artwork,
) {
    Column(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = artwork.title.value,
            style = typography.h6
        )
    }
}

@Composable
private fun ArtworksProgress(
    modifier: Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ArtworksError(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = typography.body1
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun Url.toImageRequest(): ImageRequest = ImageRequest.Builder(LocalPlatformContext.current)
    .data(toExternalValue())
    .crossfade(true)
    .build()

@Composable
private fun contentPaddingValues(): PaddingValues {
    return PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
        start = 16.dp,
        end = 16.dp,
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    )
}
