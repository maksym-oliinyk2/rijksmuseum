package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.GetArtworkUseCase
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted

internal class ArtworkDetailsViewModel(
    val key: ArtworkNavEntry,
    private val getArtworkUseCase: GetArtworkUseCase,
) : ViewModel() {

    @OptIn(ExperimentalTeaApi::class)
    private val component = Component<Message, ArtworkDetailsViewState, LoadCommand>(
        initializer = Initializer(ArtworkDetailsViewState(), LoadCommand),
        updater = { message, state -> state.update(message) },
        resolver = { snapshot, ctx ->
            snapshot.commands.forEach { _ ->
                ctx effect {
                    Message.OnDataLoaded(getArtworkUseCase.getArtwork(UrlFrom(key.id)))
                }
            }
        },
        scope = viewModelScope,
        shareOptions = ShareOptions(SharingStarted.Lazily, 1u)
    ).toStatesComponent()

    operator fun invoke(message: Flow<Message>): Flow<ArtworkDetailsViewState> = component(message)
}
