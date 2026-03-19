package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsScreen
import io.github.oliinyk.maksym.rijksmuseum.artwork.registerArtworkNavEntry
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksDestination
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksScreen
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.Navigator
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.registerArtworksNavEntry
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.RijksmuseumTheme
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val savedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            registerArtworksNavEntry()
            registerArtworkNavEntry()
        }
    }
}

@Composable
public fun App() {
    RijksmuseumTheme {
        // val backStack = rememberNavBackStack(savedStateConfig, ArtworksNavEntry)
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
