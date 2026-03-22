package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails

import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.data.ArtworkRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.ArtworkRepository
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.GetArtworkUseCase
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsViewModel
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsViewState
import org.koin.core.module.dsl.scopedOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.viewmodel.scope.viewModelScope

internal val DetailsModule = module {
    viewModel {
        ArtworkDetailsViewModel(ArtworkDetailsViewState.Initial(it.get<ArtworkDetailsDestination>().artwork), get())
    }

    viewModelScope {
        scoped { ArtworkRepositoryImpl(get()) } bind ArtworkRepository::class
        scopedOf(::GetArtworkUseCase)
    }
}
