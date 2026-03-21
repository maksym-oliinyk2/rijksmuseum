package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.GetArtworkUseCase
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import org.koin.viewmodel.scope.ScopeViewModel

internal class ArtworkDetailsViewModel(
    key: ArtworkDetailsDestination
) : ScopeViewModel() {

    private val getArtworkUseCase: GetArtworkUseCase by scope.inject()

    private val component = Component<Message, ArtworkDetailsViewState, LoadCommand>(
        // todo inject initializer
        initializer = Initializer(ArtworkDetailsViewState(key.id), LoadCommand(key.id)),
        updater = { message, state -> state.update(message) },
        resolver = { snapshot, ctx ->
            snapshot.commands.forEach { _ ->
                ctx effect {
                    Message.OnDataLoaded(getArtworkUseCase.getArtwork(key.id))
                }
            }
        },
        scope = viewModelScope,
        shareOptions = ShareOptions(SharingStarted.Lazily, 1u)
    ).toStatesComponent()

    operator fun invoke(message: Flow<Message>): Flow<ArtworkDetailsViewState> = component(message)
}
