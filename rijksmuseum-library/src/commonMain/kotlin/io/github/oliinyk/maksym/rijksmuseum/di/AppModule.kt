package io.github.oliinyk.maksym.rijksmuseum.di

import io.github.oliinyk.maksym.rijksmuseum.artworks.details.ArtworkDetailsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val appModule = module {
    viewModelOf(::ArtworkDetailsViewModel)
}
