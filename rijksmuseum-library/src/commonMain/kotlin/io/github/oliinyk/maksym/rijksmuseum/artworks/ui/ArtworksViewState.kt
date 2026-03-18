package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Page
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isIdle
import io.github.oliinyk.maksym.rijksmuseum.ui.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toException
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toIdle
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toLoading
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toLoadingNextPage
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toRefreshing
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import kotlin.jvm.JvmInline

internal sealed interface Message {
    data object OnLoadNext : Message
    data object OnReload : Message
    data object OnRefresh : Message

    @JvmInline
    value class OnDataLoaded(
        val result: Either<AppException, Page<Artwork>>
    ) : Message
}

internal data class ArtworksViewState(
    val artworks: Paginateable<Artwork> = Paginateable.loadingList(),
)

@JvmInline
internal value class LoadCommand(val paging: Paging)

internal fun update(message: Message, state: ArtworksViewState): Update<ArtworksViewState, LoadCommand> {
    return when (message) {
        is Message.OnDataLoaded -> state.onLoaded(message.result)
        Message.OnLoadNext -> state.onLoadNext()
        Message.OnRefresh -> state.onRefresh()
        Message.OnReload -> state.onReload()
    }
}

private fun ArtworksViewState.onLoadNext(): Update<ArtworksViewState, LoadCommand> {
    return if (artworks.hasMore && artworks.isIdle) {
        copy(artworks = artworks.toLoadingNextPage())
            .command(LoadCommand(Paging(artworks.data.size)))
    } else {
        noCommand()
    }
}

private fun ArtworksViewState.onReload(): Update<ArtworksViewState, LoadCommand> {
    return copy(artworks = artworks.toLoading())
        .command(LoadCommand(Paging.FirstPage))
}

private fun ArtworksViewState.onRefresh(): Update<ArtworksViewState, LoadCommand> {
    return if (artworks.isRefreshable) {
        copy(artworks = artworks.toRefreshing())
            .command(LoadCommand(Paging.FirstPage))
    } else {
        noCommand()
    }
}

private fun ArtworksViewState.onLoaded(
    result: Either<AppException, Page<Artwork>>
): Update<ArtworksViewState, LoadCommand> {
    val updated = result.fold(
        { artworks.toException(it) },
        { artworks.toIdle(it) }
    )
    return copy(artworks = updated).noCommand()
}
