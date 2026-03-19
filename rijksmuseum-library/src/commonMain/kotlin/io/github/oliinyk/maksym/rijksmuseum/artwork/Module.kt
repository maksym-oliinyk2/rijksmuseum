package io.github.oliinyk.maksym.rijksmuseum.artwork

import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepository
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.GetArtworkUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal val detailsModule = module {
    single { ArtworkRepositoryImpl(get(), get(named<Artwork>())) } bind ArtworkRepository::class
    singleOf(::GetArtworkUseCase)
    viewModelOf(::ArtworkDetailsViewModel)
}
