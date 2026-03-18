package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

@Serializable
internal data class ArtworkNavEntry(val id: String) : NavKey

internal fun PolymorphicModuleBuilder<NavKey>.registerArtworkNavEntry() {
    subclass(ArtworkNavEntry::class, ArtworkNavEntry.serializer())
}
