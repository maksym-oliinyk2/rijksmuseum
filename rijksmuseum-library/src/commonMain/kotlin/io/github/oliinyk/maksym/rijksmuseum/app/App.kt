package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.oliinyk.maksym.rijksmuseum.BuildConfig
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsScreen
import io.github.oliinyk.maksym.rijksmuseum.artwork.registerArtworkNavEntry
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksDestination
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksScreen
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.Navigator
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.registerArtworksNavEntry
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.RijksmuseumTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.dsl.KoinConfiguration

private val SavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            registerArtworksNavEntry()
            registerArtworkNavEntry()
        }
    }
}

internal typealias MessageHandler<M> = (M) -> Unit

@Composable
internal fun <M> rememberMessageHandler(
    input: suspend (M) -> Unit,
): MessageHandler<M> {
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }

    return remember(scope, input) {
        { scope.launch { input(it) } }
    }
}

@Composable
internal fun App(
    logLevel: Level = if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE,
    configurationProvider: (NavBackStack<NavKey>) -> KoinConfiguration,
) {
    // there won't be a memory leak here to pass navBackStack to koin configuration. The koin
    // configuration is retained per process (basically stored as a static field inside Koin) and won't
    // be retrieved again until the process is killed.
    // In the latter case it will be restored from Bundle and passed to a new Koin instance.
    // Also, we need to make sure we don't reference non-singletons inside nav entries!
    // see CompositionKoinApplicationLoader
    KoinApplication(
        // todo check if rememberUpdatedState is needed here
        logLevel = logLevel,
        configuration = configurationProvider(
            rememberNavBackStack(SavedStateConfig, ArtworksDestination)
        )
    ) {
        RijksmuseumTheme {
            val navigator = koinInject<Navigator>()

            NavDisplay(
                backStack = navigator,
                onBack = { navigator.navigateBack() },
                // In order to add the `ViewModelStoreNavEntryDecorator` (see comment below for why)
                // we also need to add the default `NavEntryDecorator`s as well. These provide
                // extra information to the entry's content to enable it to display correctly
                // and save its state.
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<ArtworksDestination> {
                        ArtworksScreen(viewModel = koinViewModel())
                    }
                    entry<ArtworkDetailsDestination> { key ->
                        ArtworkDetailsScreen(viewModel = koinViewModel { parametersOf(key) })
                    }
                }
            )
        }
    }
}
