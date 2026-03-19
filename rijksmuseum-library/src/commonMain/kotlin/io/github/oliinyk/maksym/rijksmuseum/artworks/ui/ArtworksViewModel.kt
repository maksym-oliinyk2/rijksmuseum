package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted

internal class ArtworksViewModel(
    private val searchUseCase: SearchUseCase,
    initialState: ArtworksViewState = ArtworksViewState(),
) : ViewModel() {

    @OptIn(ExperimentalTeaApi::class)
    private val component = Component<Message, ArtworksViewState, LoadCommand>(
        initializer = Initializer(initialState, LoadCommand(Paging.FirstPage)),
        updater = { message, state -> state.update(message) },
        resolver = { snapshot, ctx ->
            snapshot.commands.forEach { command ->
                ctx effect { Message.OnDataLoaded(searchUseCase.searchArtworks(command.paging)) }
            }
        },
        scope = viewModelScope,
        shareOptions = ShareOptions(SharingStarted.Lazily, 1u)
    ).toStatesComponent()

    operator fun invoke(message: Flow<Message>): Flow<ArtworksViewState> = component(message)

}
