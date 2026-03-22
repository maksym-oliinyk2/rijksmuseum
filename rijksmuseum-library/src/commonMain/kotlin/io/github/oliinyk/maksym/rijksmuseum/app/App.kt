package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.oliinyk.maksym.rijksmuseum.BuildConfig
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav.NavSavedStateConfig
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav.Navigator
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.theme.RijksmuseumTheme
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsScreen
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksDestination
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksScreen
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.dsl.KoinConfiguration

@Composable
internal fun RijksmuseumApp(
    logLevel: Level = if (BuildConfig.Debug) Level.DEBUG else Level.NONE,
    configurationProvider: (NavBackStack<NavKey>) -> KoinConfiguration,
) {
    // there won't be a memory leak here to pass navBackStack to koin configuration. The koin
    // configuration is retained per process (basically stored as a static field inside Koin) and won't
    // be retrieved again until the process is killed.
    // In the latter case it will be restored from Bundle and passed to a new Koin instance.
    // Also, we need to make sure we don't reference non-singletons inside nav entries!
    // see CompositionKoinApplicationLoader
    KoinApplication(
        logLevel = logLevel,
        configuration = configurationProvider(
            rememberNavBackStack(NavSavedStateConfig, ArtworksDestination)
        )
    ) {
        RijksmuseumTheme {
            val navigator = koinInject<Navigator>()

            NavDisplay(
                backStack = navigator,
                onBack = { navigator.navigateBack() },
                transitionSpec = AppTransitionSpec(),
                popTransitionSpec = AppPopTransitionSpec(),
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
