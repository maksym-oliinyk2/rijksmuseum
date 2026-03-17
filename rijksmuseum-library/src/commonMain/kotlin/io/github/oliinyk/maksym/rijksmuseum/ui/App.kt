package io.github.oliinyk.maksym.rijksmuseum.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.oliinyk.maksym.rijksmuseum.artworks.details.ArtworkDetailsScreen
import io.github.oliinyk.maksym.rijksmuseum.search.list.ArtworksScreen
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.RijksmuseumTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
internal data object RouteA : NavKey

@Serializable
internal data class RouteB(val id: String) : NavKey

private val savedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(RouteA::class, RouteA.serializer())
            subclass(RouteB::class, RouteB.serializer())
        }
    }
}

@Composable
public fun App() {
    RijksmuseumTheme {
        val backStack = rememberNavBackStack(savedStateConfig, RouteA)

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
                entry<RouteA> {
                    ArtworksScreen(onDetails = { backStack.add(RouteB("$it")) })
                }
                entry<RouteB> { key ->
                    ArtworkDetailsScreen(key)
                }
            }
        )
    }
}
