package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation

import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav.Navigator
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.domain.ArtworksUseCase
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksCommand.LoadCommand
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksCommand.NavigateToDetails
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.sideEffect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.flow.Flow
import org.koin.viewmodel.scope.ScopeViewModel

internal class ArtworksViewModel(
    initializer: Initializer<ArtworksViewState, ArtworksCommand>,
    shareOptions: ShareOptions,
) : ScopeViewModel() {

    private val artworksUseCase: ArtworksUseCase by scope.inject()
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
                    Message.OnDataLoaded(artworksUseCase.fetchArtworks(command.paging))
                }

                is NavigateToDetails -> ctx sideEffect {
                    navigator.navigateToDetails(command.artwork)
                }
            }
        }
    }
}
