package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.GettyAatType
import io.github.oliinyk.maksym.rijksmuseum.core.domain.LinguisticObject
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.domain.displayMessage
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.RoundedIconButton
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.contentPaddingValues
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Loadable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.isRefreshing
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme.RijksmuseumTheme
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme.paddings
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.toImageRequest
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.artwork_details_navigate_back
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.stringResource

internal const val ArtworkDetailsScreenTag = "Artwork details screen"
internal const val ArtworkDetailsContentTag = "Artwork details content"
internal const val ArtworkDetailsRefreshIndicatorTag = "Artwork details refresh indicator"
internal const val ArtworkDetailsExceptionIndicatorTag = "Artwork details exception indicator"

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
            state = currentState,
            onRefresh = { messageHandle(Message.OnRefresh) },
            onBack = { messageHandle(Message.OnBack) },
            modifier = modifier,
        )
    }
}

@Composable
internal fun ArtworkDetailsContent(
    state: ArtworkDetailsViewState,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = rememberPullRefreshState(
        refreshing = state.loadable.isRefreshing,
        onRefresh = onRefresh,
    )
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = modifier.testTag(ArtworkDetailsScreenTag),
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag(ArtworkDetailsExceptionIndicatorTag),
                hostState = it,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .pullRefresh(refreshState, state.loadable.isRefreshable),
            contentAlignment = Alignment.TopCenter
        ) {
            val loadableState = state.loadable.state
            if (loadableState is Loadable.Exception) {
                val displayMessage = loadableState.exception.displayMessage

                LaunchedEffect(loadableState) {
                    scaffoldState.snackbarHostState.showSnackbar(displayMessage)
                }
            }

            ArtworkDetails(
                modifier = Modifier.fillMaxSize(),
                artwork = state.loadable.data
            )

            PullRefreshIndicator(
                modifier = Modifier
                    .statusBarsPadding()
                    .testTag(ArtworkDetailsRefreshIndicatorTag),
                refreshing = state.loadable.isRefreshing,
                state = refreshState,
            )

            RoundedIconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(MaterialTheme.paddings.normal),
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(Res.string.artwork_details_navigate_back),
                onClick = onBack,
            )
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
                text = artwork.title,
                style = MaterialTheme.typography.h5
            )
        }

        items(
            items = artwork.linguisticObjects,
        ) { linguisticObject ->
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)) {
                Text(
                    text = stringResource(linguisticObject.type.displayName),
                    style = MaterialTheme.typography.subtitle1
                )
                linguisticObject.descriptions.fastForEach { description ->
                    Text(
                        text = description,
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
                loadable = Loadable.idleSingle(
                    Artwork(
                        url = UrlFrom("https://www.rijksmuseum.nl/en/collection/SK-A-4691"),
                        title = "The Night Watch",
                        primaryImage = UrlFrom("https://lh3.googleusercontent.com/nightwatch"),
                        linguisticObjects = listOf(
                            LinguisticObject(
                                type = GettyAatType.Description,
                                descriptions = NonEmptyList.of(
                                    "Militia Company of District II under the Command of Captain Frans Banninck Cocq, " +
                                        "known as the ‘Night Watch’"
                                )
                            )
                        )
                    )
                )
            ),
            onRefresh = {},
            onBack = {},
        )
    }
}
