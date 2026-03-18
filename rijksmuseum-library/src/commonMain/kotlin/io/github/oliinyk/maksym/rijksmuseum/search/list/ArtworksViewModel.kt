package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.artworks.isIdle
import io.github.oliinyk.maksym.rijksmuseum.artworks.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.artworks.toException
import io.github.oliinyk.maksym.rijksmuseum.artworks.toIdle
import io.github.oliinyk.maksym.rijksmuseum.artworks.toLoading
import io.github.oliinyk.maksym.rijksmuseum.artworks.toLoadingNextPage
import io.github.oliinyk.maksym.rijksmuseum.artworks.toRefreshing
import io.github.oliinyk.maksym.rijksmuseum.search.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.search.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.search.domain.SearchUseCase
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.flow.SharingStarted

internal class ArtworksViewModel(
    private val searchUseCase: SearchUseCase,
    initialState: ViewState = ViewState(),
) : ViewModel() {

    @OptIn(ExperimentalTeaApi::class)
    val component = Component(
        initializer = Initializer(initialState, LoadCommand(Paging.FirstPage)),
        updater = ::update,
        resolver = { snapshot, ctx ->
            snapshot.commands.forEach { command ->
                ctx effect { Message.OnDataLoaded(searchUseCase.searchArtworks(command.paging)) }
            }
        },
        scope = viewModelScope,
        shareOptions = ShareOptions(SharingStarted.Lazily, 1u)
    ).toStatesComponent()

}

internal fun update(message: Message, state: ViewState): Update<ViewState, LoadCommand> {
    return when (message) {
        is Message.OnDataLoaded -> state.onLoaded(message.result)
        Message.OnLoadNext -> state.onLoadNext()
        Message.OnRefresh -> state.onRefresh()
        Message.OnReload -> state.onReload()
    }
}

private fun ViewState.onLoadNext(): Update<ViewState, LoadCommand> {
    return if (artworks.hasMore && artworks.isIdle) {
        copy(artworks = artworks.toLoadingNextPage())
            .command(LoadCommand(Paging(artworks.data.size)))
    } else {
        noCommand()
    }
}

private fun ViewState.onReload(): Update<ViewState, LoadCommand> {
    return copy(artworks = artworks.toLoading())
            .command(LoadCommand(Paging.FirstPage))
}

private fun ViewState.onRefresh(): Update<ViewState, LoadCommand> {
    return if (artworks.isRefreshable) {
        copy(artworks = artworks.toRefreshing())
            .command(LoadCommand(Paging.FirstPage))
    } else {
        noCommand()
    }
}

private fun ViewState.onLoaded(
    result: Either<AppException, Page<Artwork>>
): Update<ViewState, LoadCommand> {
    val updated = result.fold(
        { artworks.toException(it) },
        { artworks.toIdle(it) }
    )
    return copy(artworks = updated).noCommand()
}

internal data class LoadCommand(val paging: Paging)

internal sealed interface Message {
    data object OnLoadNext : Message
    data object OnReload : Message
    data object OnRefresh : Message
    data class OnDataLoaded(val result: Either<AppException, Page<Artwork>>) : Message
}

internal data class ViewState(
    val artworks: Paginateable<Artwork> = Paginateable.loadingList(),
)
