package io.github.oliinyk.maksym.rijksmuseum.artwork

import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepository
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.GetArtworkUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val detailsModule = module {
    factoryOf(::ArtworkRepositoryImpl) bind ArtworkRepository::class
    factoryOf(::GetArtworkUseCase)
    viewModelOf(::ArtworkDetailsViewModel)
}
