package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artworks.Loadable
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.artworks.toException
import io.github.oliinyk.maksym.rijksmuseum.artworks.toIdle
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ArtworksViewModel(private val searchUseCase: SearchUseCase) : ViewModel() {

    private val _state = MutableStateFlow(ViewState())
    val state: Flow<ViewState> by ::_state

    init {
        viewModelScope.launch {
            _state.update { state ->
                val nextState = searchUseCase
                    .searchArtworks(null)
                    .fold({
                        state.artworks.toException(it)
                    }, {
                        val artworks = state.artworks

                        it.data.take(ViewState.ViewPortSize).forEachIndexed { index, url ->
                            async {
                                searchUseCase.fetchArtworkDetails(url)
                                    .fold({

                                    }, {
                                        val globalIndex = state.artworks.data.lastIndex + index

                                        _state.value.copy()
                                    })
                            }
                        }

                        val loadables = it.data.map { url ->
                            LoadableArtwork(url, Loadable(null, Loadable.Idle))
                        }

                        artworks.toIdle(loadables, it.next)
                    })

                state.copy(artworks = nextState)
            }
        }
    }

    fun onLoadMore() {

    }

}

internal data class ViewState(
    val artworks: Paginateable<LoadableArtwork> = Paginateable.loadingList(),
) {
    companion object {
        const val ViewPortSize = 20
    }
}

internal data class LoadableArtwork(
    val url: Url,
    val loadable: Loadable<Artwork?>,
)