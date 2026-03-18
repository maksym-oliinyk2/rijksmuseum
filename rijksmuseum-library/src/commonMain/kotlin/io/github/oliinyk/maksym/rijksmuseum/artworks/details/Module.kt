package io.github.oliinyk.maksym.rijksmuseum.artworks.details

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val detailsModule = module {
    viewModelOf(::ArtworkDetailsViewModel)
}
