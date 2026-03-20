package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.oliinyk.maksym.rijksmuseum.app.rememberMessageHandler
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.displayMessage
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_action_retry
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_image_description
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_no_data_message
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshing
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.paddings
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ArtworksScreen(
    modifier: Modifier = Modifier,
    viewModel: ArtworksViewModel,
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val component = remember(viewModel) { viewModel(messages) }
    val state by component.collectAsStateWithLifecycle(null)
    val currentState = state

    if (currentState != null) {
        val messageHandle = rememberMessageHandler(messages::emit)

        ArtworksContent(
            modifier = modifier,
            state = currentState,
            onRefresh = { messageHandle(Message.OnRefresh) },
            onReload = { messageHandle(Message.OnReload) },
            onLoadNext = { messageHandle(Message.OnLoadNext) },
            onNavigateToDetails = { messageHandle(Message.OnNavigateToDetails(it)) }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
internal fun ArtworksContent(
    state: ArtworksViewState,
    onRefresh: () -> Unit,
    onReload: () -> Unit,
    onLoadNext: () -> Unit,
    onNavigateToDetails: (Artwork) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = rememberPullRefreshState(
        refreshing = state.artworks.isRefreshing,
        onRefresh = onRefresh,
    )

    Scaffold(
        modifier = modifier.navigationBarsPadding(),
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .pullRefresh(refreshState, state.artworks.isRefreshable),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTag = "ArtworksList" },
                contentPadding = contentPaddingValues(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.normal),
            ) {
                if (state.artworks.data.isNotEmpty()) {
                    artworkItems(state.artworks, onNavigateToDetails)
                }

                paginateableContent(
                    paginateable = state.artworks,
                    onRefresh = onRefresh,
                    onReload = onReload,
                    onLoadNext = onLoadNext,
                )

                item {
                    LaunchedEffect(Unit) {
                        onLoadNext()
                    }
                }
            }

            PullRefreshIndicator(
                modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
                refreshing = state.artworks.isRefreshing,
                state = refreshState,
            )
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
    onRefresh: () -> Unit,
    onReload: () -> Unit,
    onLoadNext: () -> Unit,
) = item(
    key = paginateable.state::class.simpleName,
    contentType = paginateable.state::class
) {
    when (val state = paginateable.state) {
        is Paginateable.Exception ->
            ArtworksError(
                modifier = if (paginateable.data.isEmpty()) {
                    Modifier.fillParentMaxSize()
                } else {
                    Modifier.fillParentMaxWidth()
                },
                message = state.exception.displayMessage,
                onRetry = if (paginateable.data.isEmpty()) onReload else onLoadNext
            )

        is Paginateable.Loading -> ProgressIndicator(modifier = Modifier.fillParentMaxSize())
        is Paginateable.LoadingNext -> ProgressIndicator(modifier = Modifier.fillParentMaxWidth())
        is Paginateable.Idle, is Paginateable.Refreshing -> {
            if (paginateable.data.isEmpty()) {
                ArtworksError(
                    modifier = Modifier.fillParentMaxSize(),
                    message = stringResource(Res.string.artworks_no_data_message),
                    onRetry = onRefresh
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
        modifier = Modifier.semantics(mergeDescendants = true) {
            testTag = artwork.title.value
        },
        elevation = MaterialTheme.paddings.small,
        shape = RoundedCornerShape(MaterialTheme.paddings.medium),
        onClick = onClick
    ) {
        Column {
            ArtworkImage(imageUrl = artwork.images.firstOrNull())
            Spacer(modifier = Modifier.height(MaterialTheme.paddings.medium))
            ArtworkContents(artwork = artwork)
            Spacer(modifier = Modifier.height(MaterialTheme.paddings.medium))
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
        shape = RoundedCornerShape(
            topStart = MaterialTheme.paddings.medium,
            topEnd = MaterialTheme.paddings.medium
        ),
        color = colors.onSurface.copy(alpha = 0.2f)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl.toImageRequest(),
                contentDescription = stringResource(Res.string.artworks_image_description),
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
        modifier = Modifier.padding(horizontal = MaterialTheme.paddings.medium)
    ) {
        Text(
            text = artwork.title.value,
            style = typography.h6
        )
    }
}

@Composable
internal fun ProgressIndicator(
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
internal fun ArtworksError(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier.padding(MaterialTheme.paddings.normal),
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

@Composable
internal fun Url.toImageRequest(): ImageRequest = ImageRequest.Builder(LocalPlatformContext.current)
    .data(toExternalValue())
    .crossfade(true)
    .build()

@Composable
private fun contentPaddingValues(): PaddingValues {
    return PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding() + MaterialTheme.paddings.normal,
        start = MaterialTheme.paddings.normal,
        end = MaterialTheme.paddings.normal,
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    )
}
