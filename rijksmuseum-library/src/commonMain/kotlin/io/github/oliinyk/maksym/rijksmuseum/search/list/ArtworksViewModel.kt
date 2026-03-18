package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.artworks.Page
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.artworks.toException
import io.github.oliinyk.maksym.rijksmuseum.artworks.toIdle
import io.github.oliinyk.maksym.rijksmuseum.search.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.search.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.search.domain.SearchUseCase
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.flow.SharingStarted

internal class ArtworksViewModel(
    private val searchUseCase: SearchUseCase,
    initialState: ViewState = ViewState(),
) : ViewModel() {

    /*private val _intents = MutableSharedFlow<Intent>()
    private val _effects = MutableSharedFlow<LoadEffect>()

    val state = flow {
        var state = initialState

        _intents.collect { intent ->
            val (nextState, effect) = reduce(intent, state)
            state = nextState
            _effects.emit(effect)
            emit(nextState)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, initialState)*/

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
        Message.OnLoadNext -> TODO()
        Message.OnRefresh -> TODO()
    }
}

private fun ViewState.onLoaded(
    result: Either<AppException, Page<Artwork>>
): Update<ViewState, Nothing> {
    val updated = result.fold(
        { artworks.toException(it) },
        { artworks.toIdle(it) }
    )
    return copy(artworks = updated).noCommand()
}

internal data class LoadCommand(val paging: Paging)

internal sealed interface Message {
    data object OnLoadNext : Message
    data object OnRefresh : Message
    data class OnDataLoaded(val result: Either<AppException, Page<Artwork>>) : Message
}

internal data class ViewState(
    val artworks: Paginateable<Artwork> = Paginateable.loadingList(),
)
