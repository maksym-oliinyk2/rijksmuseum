package io.github.oliinyk.maksym.rijksmuseum.search.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

internal class ArtworksViewModel(private val searchUseCase: SearchUseCase) : ViewModel() {

    init {
        viewModelScope.launch {
            val artworks = searchUseCase.searchArtworks()

            println(artworks)
        }
    }

}
