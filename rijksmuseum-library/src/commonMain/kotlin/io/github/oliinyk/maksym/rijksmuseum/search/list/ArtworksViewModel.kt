package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.artworks.Paging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class ArtworksViewModel(private val searchUseCase: SearchUseCase) : ViewModel() {

    private val _state = MutableStateFlow(ViewState())
    val state: Flow<ViewState> by ::_state

    init {
        viewModelScope.launch {
            //   _state.update { state ->
                val nextState = searchUseCase
                    .searchArtworks(Paging.FirstPage)
                    .fold({
                        it.printStackTrace()
                        //state.artworks.toException(it)
                    }, {

                        println("Art $it")
                    })

            //   state.copy(artworks = nextState)
            //   }
        }
    }

    fun onLoadMore() {

    }

}

internal data class ViewState(
    val artworks: Paginateable<Artwork> = Paginateable.loadingList(),
)