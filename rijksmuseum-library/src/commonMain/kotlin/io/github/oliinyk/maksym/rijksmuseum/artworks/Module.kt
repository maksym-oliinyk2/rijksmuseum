package io.github.oliinyk.maksym.rijksmuseum.artworks

import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchApiImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepository
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.KoinViewModelScopeApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.scopedOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.viewmodel.scope.viewModelScope

@OptIn(KoinExperimentalAPI::class, KoinViewModelScopeApi::class)
internal val searchModule: Module = module {
    viewModelOf(::ArtworksViewModel)

    viewModelScope {
        scopedOf(::SearchApiImpl).bind(SearchApi::class)
        scoped { SearchRepositoryImpl(get()) }.bind(SearchRepository::class)
        scopedOf(::SearchUseCase)
    }
}
