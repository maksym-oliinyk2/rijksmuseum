package io.github.oliinyk.maksym.rijksmuseum.artwork

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val detailsModule = module {
    viewModelOf(::ArtworkDetailsViewModel)
}
