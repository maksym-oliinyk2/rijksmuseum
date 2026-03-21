package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksCommand.LoadCommand
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paging
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.sideEffect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.withContext
import org.koin.viewmodel.scope.ScopeViewModel

internal class ArtworksViewModel : ScopeViewModel() {

    private val searchUseCase: SearchUseCase by scope.inject()
    private val navigator: Navigator by scope.inject()

    private val component = Component<Message, ArtworksViewState, ArtworksCommand>(
        initializer = Initializer(ArtworksViewState(), LoadCommand(Paging.FirstPage)),
        updater = { message, state -> state.update(message) },
        resolver = { snapshot, ctx ->
            snapshot.commands.forEach { command ->
                when (command) {
                    is LoadCommand -> ctx effect { Message.OnDataLoaded(searchUseCase.searchArtworks(command.paging)) }
                    is ArtworksCommand.NavigateToDetails -> ctx sideEffect {
                        withContext(Dispatchers.Main.immediate) {
                            navigator.navigateToDetails(command.artwork)
                        }
                    }
                }
            }
        },
        scope = viewModelScope,
        shareOptions = ShareOptions(SharingStarted.Lazily, 1u)
    ).toStatesComponent()

    operator fun invoke(message: Flow<Message>): Flow<ArtworksViewState> = component(message)
}
