package io.github.oliinyk.maksym.rijksmuseum.feature.artworks

import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.domain.SearchRepository
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksViewModel
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksViewState
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.viewmodel.scope.viewModelScope

internal val SearchModule: Module = module {
    viewModel { ArtworksViewModel(ArtworksViewState.Initial(), get()) }

    viewModelScope {
        scoped { SearchRepositoryImpl(get()) }.bind(SearchRepository::class)
        scoped { SearchUseCase(get()) }
    }
}
