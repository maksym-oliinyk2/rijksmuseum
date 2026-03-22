package io.github.oliinyk.maksym.rijksmuseum.artwork

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toException
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toIdle
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toRefreshing
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import kotlin.jvm.JvmInline

internal sealed interface Message {
    data object OnReload : Message
    data object OnRefresh : Message

    @JvmInline
    value class OnDataLoaded(
        val result: Either<AppException, Artwork>
    ) : Message
}

internal data class ArtworkDetailsViewState(
    val artwork: Loadable<Artwork>,
) {
    companion object {
        fun Initial(artwork: Artwork): Initializer<ArtworkDetailsViewState, LoadCommand> =
            Initializer(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), emptySet<LoadCommand>())
    }
}

@JvmInline
internal value class LoadCommand(
    val artworkId: Url,
)

internal fun ArtworkDetailsViewState.update(message: Message): Update<ArtworkDetailsViewState, LoadCommand> {
    return when (message) {
        is Message.OnDataLoaded -> onLoaded(message.result)
        Message.OnRefresh -> onRefresh()
        Message.OnReload -> onReload()
    }
}

private fun ArtworkDetailsViewState.onReload(): Update<ArtworkDetailsViewState, LoadCommand> {
    return copy(artwork = Loadable(artwork.data, Loadable.Loading))
        .command(LoadCommand(artwork.data.url))
}

private fun ArtworkDetailsViewState.onRefresh(): Update<ArtworkDetailsViewState, LoadCommand> {
    return if (artwork.isRefreshable) {
        copy(artwork = artwork.toRefreshing())
            .command(LoadCommand(artwork.data.url))
    } else {
        noCommand()
    }
}

private fun ArtworkDetailsViewState.onLoaded(
    result: Either<AppException, Artwork>
): Update<ArtworkDetailsViewState, LoadCommand> {
    val updated = result.fold(
        { artwork.toException(it) },
        { artwork.toIdle(it) }
    )
    return copy(artwork = updated).noCommand()
}
