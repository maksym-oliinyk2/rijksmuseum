package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.domain.displayMessage
import io.github.oliinyk.maksym.rijksmuseum.core.domain.toStringValue
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.DisplayMessage
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.ProgressIndicator
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.contentPaddingValues
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.canLoadNextForIndex
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.isLoading
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.isRefreshing
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme.RijksmuseumTheme
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme.paddings
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.toImageRequest
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksViewState.Companion.StartPreloadBeforeItems
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_image_description
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_no_data_message
import io.github.oliinyk.maksym.rijksmuseum.res.artworks_shimmer_items
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

internal const val ArtworksScreenTag = "Artworks screen"
internal const val ArtworksScrollContainerTag = "Artworks scroll container"
internal const val ArtworksShimmerItemTag = "Artworks shimmer item"
private val CardImageHeight = 200.dp
private const val ShimmerDurationMillis = 1000
private const val ShimmerPeakAlpha = 0.7f
private const val ShimmerPeakAtMillis = 500

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
            val shimmerTitles = stringArrayResource(Res.array.artworks_shimmer_items)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(ArtworksScrollContainerTag),
                contentPadding = contentPaddingValues(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.normal),
                userScrollEnabled = !state.artworks.isLoading,
            ) {
                if (state.artworks.data.isNotEmpty()) {
                    artworkItems(
                        paginateable = state.artworks,
                        onNavigateToDetails = { onMessage(Message.OnNavigateToDetails(it)) },
                        onLoadNext = { onMessage(Message.OnLoadNext) },
                    )
                }

                paginateableContent(
                    paginateable = state.artworks,
                    shimmerTitles = shimmerTitles,
                    onRefresh = { onMessage(Message.OnRefresh) },
                    onReload = { onMessage(Message.OnReload) },
                    onLoadNext = { onMessage(Message.OnLoadNext) }
                )
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
    onLoadNext: () -> Unit,
) {
    itemsIndexed(
        items = paginateable.data,
        key = { _, item -> item.url.toStringValue() },
    ) { i, artwork ->
        ArtworkCard(
            artwork = artwork,
            onClick = { onNavigateToDetails(artwork) }
        )

        if (paginateable.canLoadNextForIndex(i, StartPreloadBeforeItems)) {
            LaunchedEffect(Unit) {
                onLoadNext()
            }
        }
    }
}

private fun LazyListScope.paginateableContent(
    paginateable: Paginateable<Artwork>,
    shimmerTitles: List<String>,
    onRefresh: () -> Unit,
    onReload: () -> Unit,
    onLoadNext: () -> Unit,
) {
    when (val state = paginateable.state) {
        is Paginateable.Exception -> item(
            key = paginateable.state::class.simpleName,
            contentType = paginateable.state::class
        ) {
            DisplayMessage(
                modifier = if (paginateable.data.isEmpty()) {
                    Modifier.fillParentMaxSize()
                } else {
                    Modifier.fillParentMaxWidth()
                }.padding(MaterialTheme.paddings.normal),
                imageVector = Icons.Default.Error,
                message = state.exception.displayMessage,
                onRetry = if (paginateable.data.isEmpty()) onReload else onLoadNext
            )
        }

        is Paginateable.Loading -> shimmerItems(shimmerTitles)

        is Paginateable.LoadingNext -> item(
            key = paginateable.state::class.simpleName,
            contentType = paginateable.state::class
        ) {
            ProgressIndicator(modifier = Modifier.fillParentMaxWidth())
        }

        is Paginateable.Idle, is Paginateable.Refreshing -> item(
            key = paginateable.state::class.simpleName,
            contentType = paginateable.state::class
        ) {
            if (paginateable.data.isEmpty()) {
                DisplayMessage(
                    modifier = Modifier
                        .padding(MaterialTheme.paddings.normal)
                        .fillParentMaxSize(),
                    message = stringResource(Res.string.artworks_no_data_message),
                    onRetry = onRefresh
                )
            }
        }
    }
}

private fun LazyListScope.shimmerItems(
    shimmerTitles: List<String>
) {
    repeat(shimmerTitles.size) { i ->
        item {
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = ShimmerDurationMillis
                        ShimmerPeakAlpha at ShimmerPeakAtMillis
                    },
                    repeatMode = RepeatMode.Reverse
                )
            )

            ShimmerCard(
                modifier = Modifier
                    .graphicsLayer { this.alpha = alpha }
                    .testTag(ArtworksShimmerItemTag),
                title = Title(shimmerTitles[i])
            )
        }
    }
}

@Composable
private fun ShimmerCard(
    modifier: Modifier = Modifier,
    title: Title,
) {
    ArtworkCard(
        modifier = modifier,
        title = title,
        image = null,
        onClick = { },
        enabled = false,
    )
}

@Composable
private fun ArtworkCard(
    modifier: Modifier = Modifier,
    artwork: Artwork,
    onClick: () -> Unit,
) {
    ArtworkCard(
        modifier = modifier.semantics(mergeDescendants = true) {
            testTag = artwork.title.value
        },
        title = artwork.title,
        image = artwork.primaryImage,
        onClick = onClick
    )
}

@Composable
private fun ArtworkCard(
    modifier: Modifier = Modifier,
    title: Title,
    image: Url?,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = MaterialTheme.paddings.small,
        shape = MaterialTheme.shapes.medium,
        enabled = enabled,
        onClick = onClick,
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
                if (image != null) {
                    AsyncImage(
                        model = image.toImageRequest(),
                        contentDescription = stringResource(Res.string.artworks_image_description),
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Text(
                modifier = Modifier.padding(horizontal = MaterialTheme.paddings.medium),
                text = title.value,
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
                            linguisticObjects = emptyList()
                        ),
                        Artwork(
                            url = UrlFrom("https://www.rijksmuseum.nl/en/collection/SK-A-2344"),
                            title = Title("The Milkmaid"),
                            primaryImage = UrlFrom("https://lh3.googleusercontent.com/milkmaid"),
                            linguisticObjects = emptyList()
                        )
                    )
                )
            ),
            onMessage = {}
        )
    }
}
