package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksCommand.LoadCommand
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksCommand.NavigateToDetails
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.sideEffect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.koin.viewmodel.scope.ScopeViewModel

internal class ArtworksViewModel(
    initializer: Initializer<ArtworksViewState, ArtworksCommand>,
    shareOptions: ShareOptions,
) : ScopeViewModel() {

    private val searchUseCase: SearchUseCase by scope.inject()
    private val navigator: Navigator by scope.inject()
    private val component = Component(
        initializer = initializer,
        updater = { message, state -> state.update(message) },
        resolver = ::resolveEffects,
        scope = viewModelScope,
        shareOptions = shareOptions,
    ).toStatesComponent()

    operator fun invoke(message: Flow<Message>): Flow<ArtworksViewState> = component(message)

    private fun resolveEffects(
        snapshot: Snapshot<Message, ArtworksViewState, ArtworksCommand>,
        ctx: ResolveCtx<Message>
    ) {
        snapshot.commands.forEach { command ->
            when (command) {
                is LoadCommand -> ctx effect {
                    Message.OnDataLoaded(searchUseCase.searchArtworks(command.paging))
                }

                is NavigateToDetails -> ctx sideEffect {
                    withContext(Dispatchers.Main.immediate) {
                        navigator.navigateToDetails(command.artwork)
                    }
                }
            }
        }
    }
}
