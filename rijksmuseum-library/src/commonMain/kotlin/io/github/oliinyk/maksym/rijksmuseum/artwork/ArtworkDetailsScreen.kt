package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.NonEmptyList
import coil3.compose.AsyncImage
import io.github.oliinyk.maksym.rijksmuseum.app.rememberMessageHandler
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Description
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.LinguisticObject
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.GettyAatType
import io.github.oliinyk.maksym.rijksmuseum.artworks.displayMessage
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.artwork_details_no_data
import io.github.oliinyk.maksym.rijksmuseum.ui.common.DisplayMessage
import io.github.oliinyk.maksym.rijksmuseum.ui.common.ProgressIndicator
import io.github.oliinyk.maksym.rijksmuseum.ui.common.contentPaddingValues
import io.github.oliinyk.maksym.rijksmuseum.ui.common.toImageRequest
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshing
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.RijksmuseumTheme
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.paddings
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.stringResource

internal const val ArtworkDetailsScreenTag = "Artwork details screen"
internal const val ArtworkDetailsContentTag = "Artwork details content"

private val TopBarImageHeight = 300.dp

@Composable
internal fun ArtworkDetailsScreen(
    viewModel: ArtworkDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val state by viewModel.invoke(messages).collectAsStateWithLifecycle(null)
    val currentState = state

    if (currentState != null) {
        val messageHandle = rememberMessageHandler(messages::emit)

        ArtworkDetailsContent(
            modifier = modifier,
            state = currentState,
            onRefresh = { messageHandle(Message.OnRefresh) },
            onReload = { messageHandle(Message.OnReload) },
        )
    }
}

@Composable
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
        modifier = modifier.testTag(ArtworkDetailsScreenTag),
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
                modifier = Modifier.statusBarsPadding(),
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
        is Loadable.Exception -> DisplayMessage(
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
                DisplayMessage(
                    modifier = Modifier.fillMaxSize(),
                    message = stringResource(Res.string.artwork_details_no_data),
                    onRetry = onRefresh
                )
            }
        }
    }
}

@Composable
private fun ArtworkDetails(
    artwork: Artwork,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(ArtworkDetailsContentTag),
        contentPadding = contentPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.normal)
    ) {
        val topImage = artwork.primaryImage

        if (topImage != null) {
            item(key = "topImage") {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TopBarImageHeight),
                    model = topImage.toImageRequest(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            }
        }

        item(key = "title") {
            Text(
                text = artwork.title.value,
                style = MaterialTheme.typography.h5
            )
        }

        items(
            items = artwork.descriptions,
        ) { linguisticObject ->
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)) {
                Text(
                    text = stringResource(linguisticObject.type.displayName),
                    style = MaterialTheme.typography.subtitle1
                )
                linguisticObject.descriptions.fastForEach { description ->
                    Text(
                        text = description.value,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = false)
@Suppress("UnusedPrivateMember")
private fun ArtworkDetailsContentPreview() {
    RijksmuseumTheme {
        ArtworkDetailsContent(
            state = ArtworkDetailsViewState(
                artworkId = UrlFrom("https://www.rijksmuseum.nl/en/collection/SK-A-4691"),
                artwork = Loadable.idleSingle(
                    Artwork(
                        url = UrlFrom("https://www.rijksmuseum.nl/en/collection/SK-A-4691"),
                        title = Title("The Night Watch"),
                        primaryImage = UrlFrom("https://lh3.googleusercontent.com/nightwatch"),
                        descriptions = listOf(
                            LinguisticObject(
                                type = GettyAatType.Description,
                                descriptions = NonEmptyList.of(
                                    Description(
                                        "Militia Company of District II under the Command of Captain Frans Banninck Cocq, " +
                                            "known as the ‘Night Watch’"
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            onRefresh = {},
            onReload = {}
        )
    }
}
