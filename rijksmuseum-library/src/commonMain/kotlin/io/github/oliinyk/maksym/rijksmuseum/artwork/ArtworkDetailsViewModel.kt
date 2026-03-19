package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ValueHolder
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.GetArtworkUseCase
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted

internal class ArtworkDetailsViewModel(
    // todo refine injected dependencies
    private val key: ArtworkDetailsDestination,
    private val valueHolder: ValueHolder<Artwork>,
    private val getArtworkUseCase: GetArtworkUseCase,
) : ViewModel() {

    @OptIn(ExperimentalTeaApi::class)
    private val component = Component<Message, ArtworkDetailsViewState, LoadCommand>(
        // todo inject initializer
        initializer = Initializer(Dispatchers.Unconfined) {
            val artwork = valueHolder.getAndForget()

            if (artwork == null) {
                Initial(
                    currentState = ArtworkDetailsViewState(key.id),
                    commands = setOf(LoadCommand(key.id))
                )
            } else {
                Initial(
                    currentState = ArtworkDetailsViewState(
                        artworkId = key.id,
                        artwork = Loadable.idleSingle(artwork),
                    ),
                    commands = setOf()
                )
            }
        },
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
