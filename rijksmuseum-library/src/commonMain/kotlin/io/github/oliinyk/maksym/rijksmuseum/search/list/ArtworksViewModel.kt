package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import io.github.oliinyk.maksym.rijksmuseum.artworks.toException
import io.github.oliinyk.maksym.rijksmuseum.artworks.toIdle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ArtworksViewModel(private val searchUseCase: SearchUseCase) : ViewModel() {

    private val _state = MutableStateFlow(ViewState())
    val state: StateFlow<ViewState> by ::_state

    init {
        viewModelScope.launch {
            _state.update { state ->
                val nextState = searchUseCase
                    .searchArtworks(Paging.FirstPage)
                    .fold({
                        state.artworks.toException(it)
                    }, {
                        state.artworks.toIdle(it)
                    })

                state.copy(artworks = nextState)
            }
        }
    }

    fun onLoadMore() {

    }

}

internal data class ViewState(
    val artworks: Paginateable<Artwork> = Paginateable.loadingList(),
)