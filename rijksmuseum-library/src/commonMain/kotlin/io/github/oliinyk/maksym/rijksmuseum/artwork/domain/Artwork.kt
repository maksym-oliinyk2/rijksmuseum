package io.github.oliinyk.maksym.rijksmuseum.artwork.domain

import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.domain.Url

public data class Artwork(
    val url: Url,
    val title: Title,
    val images: List<Url>
)
