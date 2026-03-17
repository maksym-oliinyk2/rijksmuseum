package io.github.oliinyk.maksym.rijksmuseum.di

import io.github.oliinyk.maksym.rijksmuseum.artworks.details.detailsModule
import io.github.oliinyk.maksym.rijksmuseum.search.list.searchModule
import org.koin.dsl.module

internal val appModule = module {
    includes(searchModule, detailsModule)
}
