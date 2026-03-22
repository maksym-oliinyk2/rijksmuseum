package io.github.oliinyk.maksym.rijksmuseum.artwork

import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepository
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.GetArtworkUseCase
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
