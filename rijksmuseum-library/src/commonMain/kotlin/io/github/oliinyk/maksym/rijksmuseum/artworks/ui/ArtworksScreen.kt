package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.oliinyk.maksym.rijksmuseum.app.rememberMessageHandler
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.displayMessage
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.domain.toStringValue
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_image_description
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_no_data_message
import io.github.oliinyk.maksym.rijksmuseum.ui.common.DisplayMessage
import io.github.oliinyk.maksym.rijksmuseum.ui.common.ProgressIndicator
import io.github.oliinyk.maksym.rijksmuseum.ui.common.contentPaddingValues
import io.github.oliinyk.maksym.rijksmuseum.ui.common.toImageRequest
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshing
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.RijksmuseumTheme
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.paddings
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.stringResource

internal const val ArtworksScreenTag = "Artworks screen"
internal const val ArtworksScrollContainerTag = "Scroll container"
private val CardImageHeight = 200.dp

// todo document - more than 4 action handler lambdas -> use message handler
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
            onMessage = messageHandle
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
internal fun ArtworksContent(
    state: ArtworksViewState,
    onMessage: (Message) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = rememberPullRefreshState(
        refreshing = state.artworks.isRefreshing,
        onRefresh = { onMessage(Message.OnRefresh) },
    )

    Scaffold(
        modifier = modifier
            .navigationBarsPadding()
            .testTag(ArtworksScreenTag),
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
                    .testTag(ArtworksScrollContainerTag),
                contentPadding = contentPaddingValues(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.normal),
            ) {
                if (state.artworks.data.isNotEmpty()) {
                    artworkItems(state.artworks) {
                        onMessage(Message.OnNavigateToDetails(it))
                    }
                }

                paginateableContent(
                    paginateable = state.artworks,
                    onRefresh = { onMessage(Message.OnRefresh) },
                    onReload = { onMessage(Message.OnReload) },
                    onLoadNext = { onMessage(Message.OnLoadNext) }
                )

                item {
                    // todo play with preloading strategy
                    LaunchedEffect(Unit) {
                        onMessage(Message.OnLoadNext)
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
        key = { item -> item.url.toStringValue() },
    ) { artwork ->
        ArtworkCard(
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
            DisplayMessage(
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
                DisplayMessage(
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
private fun ArtworkCard(
    artwork: Artwork,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.semantics(mergeDescendants = true) {
            testTag = artwork.title.value
        },
        elevation = MaterialTheme.paddings.small,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(bottom = MaterialTheme.paddings.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.medium),
        ) {
            Surface(
                modifier = Modifier
                    .height(CardImageHeight)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium.copy(
                    bottomStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp)
                ),
                color = colors.onSurface.copy(alpha = 0.2f)
            ) {
                if (artwork.primaryImage != null) {
                    AsyncImage(
                        model = artwork.primaryImage.toImageRequest(),
                        contentDescription = stringResource(Res.string.artworks_image_description),
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Text(
                modifier = Modifier.padding(horizontal = MaterialTheme.paddings.medium),
                text = artwork.title.value,
                style = typography.h6,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
@Preview(showSystemUi = false)
@Suppress("UnusedPrivateMember")
private fun ArtworksContentPreview() {
    RijksmuseumTheme {
        ArtworksContent(
            state = ArtworksViewState(
                artworks = Paginateable.idleList(
                    data = listOf(
                        Artwork(
                            url = UrlFrom("https://www.rijksmuseum.nl/en/collection/SK-A-4691"),
                            title = Title("The Night Watch"),
                            primaryImage = UrlFrom("https://lh3.googleusercontent.com/nightwatch"),
                            descriptions = emptyList()
                        ),
                        Artwork(
                            url = UrlFrom("https://www.rijksmuseum.nl/en/collection/SK-A-2344"),
                            title = Title("The Milkmaid"),
                            primaryImage = UrlFrom("https://lh3.googleusercontent.com/milkmaid"),
                            descriptions = emptyList()
                        )
                    )
                )
            ),
            onMessage = {}
        )
    }
}
