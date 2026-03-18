package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsScreen
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkNavEntry
import io.github.oliinyk.maksym.rijksmuseum.artwork.registerArtworkNavEntry
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksNavEntry
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.ArtworksScreen
import io.github.oliinyk.maksym.rijksmuseum.artworks.ui.registerArtworksNavEntry
import io.github.oliinyk.maksym.rijksmuseum.domain.toExternalValue
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.RijksmuseumTheme
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

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
        val backStack = rememberNavBackStack(savedStateConfig, ArtworksNavEntry)

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            // In order to add the `ViewModelStoreNavEntryDecorator` (see comment below for why)
            // we also need to add the default `NavEntryDecorator`s as well. These provide
            // extra information to the entry's content to enable it to display correctly
            // and save its state.
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<ArtworksNavEntry> {
                    ArtworksScreen(
                        onNavigateToDetails = { artwork ->
                            backStack.add(ArtworkNavEntry(artwork.url.toExternalValue()))
                        }
                    )
                }
                entry<ArtworkNavEntry> { key ->
                    ArtworkDetailsScreen(key)
                }
            }
        )
    }
}
