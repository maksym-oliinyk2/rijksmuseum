package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.navigation3.runtime.NavKey
import io.github.oliinyk.maksym.rijksmuseum.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
internal value class ArtworkDetailsDestination(
    @Serializable(with = UrlSerializer::class)
    val id: Url
) : NavKey

internal fun PolymorphicModuleBuilder<NavKey>.registerArtworkNavEntry() {
    subclass(ArtworkDetailsDestination::class, ArtworkDetailsDestination.serializer())
}
