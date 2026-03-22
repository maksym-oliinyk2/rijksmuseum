package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav.Navigator
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.ArtworkUseCase
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.sideEffect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.flow.Flow
import org.koin.viewmodel.scope.ScopeViewModel

internal class ArtworkDetailsViewModel(
    initializer: Initializer<ArtworkDetailsViewState, Command>,
    shareOptions: ShareOptions,
) : ScopeViewModel() {
    private val artworkUseCase: ArtworkUseCase by scope.inject()
    private val navigator: Navigator by scope.inject()
    private val component = Component<Message, ArtworkDetailsViewState, Command>(
        initializer = initializer,
        updater = { message, state -> state.update(message) },
        resolver = { snapshot, ctx ->
            snapshot.commands.forEach { command ->
                when (command) {
                    is Command.LoadCommand -> ctx effect {
                        Message.OnDataLoaded(artworkUseCase.fetchArtwork(command.artworkId))
                    }
                    Command.OnBack -> ctx.sideEffect {
                        navigator.navigateBack()
                    }
                }
            }
        },
        scope = viewModelScope,
        shareOptions = shareOptions,
    ).toStatesComponent()

    operator fun invoke(message: Flow<Message>): Flow<ArtworkDetailsViewState> = component(message)
}
