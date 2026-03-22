package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.GetArtworkUseCase
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.flow.Flow
import org.koin.viewmodel.scope.ScopeViewModel

internal class ArtworkDetailsViewModel(
    initializer: Initializer<ArtworkDetailsViewState, LoadCommand>,
    shareOptions: ShareOptions,
) : ScopeViewModel() {
    private val getArtworkUseCase: GetArtworkUseCase by scope.inject()
    private val component = Component<Message, ArtworkDetailsViewState, LoadCommand>(
        initializer = initializer,
        updater = { message, state -> state.update(message) },
        resolver = { snapshot, ctx ->
            snapshot.commands.forEach { command ->
                ctx effect {
                    Message.OnDataLoaded(getArtworkUseCase.getArtwork(command.artworkId))
                }
            }
        },
        scope = viewModelScope,
        shareOptions = shareOptions,
    ).toStatesComponent()

    operator fun invoke(message: Flow<Message>): Flow<ArtworkDetailsViewState> = component(message)
}
