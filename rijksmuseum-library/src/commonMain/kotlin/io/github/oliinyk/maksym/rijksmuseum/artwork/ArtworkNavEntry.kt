package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal data class ArtworkNavEntry(val id: String) : NavKey
