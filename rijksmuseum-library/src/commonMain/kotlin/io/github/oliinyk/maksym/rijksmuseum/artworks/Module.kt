package io.github.oliinyk.maksym.rijksmuseum.artworks

import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepository
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksViewModel
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksViewState
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.viewmodel.scope.viewModelScope

internal val SearchModule: Module = module {
    viewModel { ArtworksViewModel(ArtworksViewState.Initial()) }

    viewModelScope {
        scoped { SearchRepositoryImpl(get()) }.bind(SearchRepository::class)
        scoped { SearchUseCase(get()) }
    }
}
