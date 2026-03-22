package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.oliinyk.maksym.rijksmuseum.BuildConfig
import io.github.oliinyk.maksym.rijksmuseum.core.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.core.data.RijksmuseumApiImpl
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav.Navigator
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.DetailsModule
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.SearchModule
import io.github.xlopec.tea.core.ShareOptions
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.logging.EMPTY
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.SIMPLE
import kotlinx.coroutines.flow.SharingStarted
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind

internal fun AppModule(
    backStack: NavBackStack<NavKey>,
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
): Module = module {
    includes(SearchModule, DetailsModule)
    single { if (BuildConfig.DEBUG) Logger.SIMPLE else Logger.EMPTY }
    single { if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE }
    single { HttpClient(get(), get(), engine) }
    single { Navigator(backStack) }
    single { ShareOptions(SharingStarted.Lazily, 1u) }
    single { RijksmuseumApiImpl(get()) }.bind(RijksmuseumApi::class)
}
