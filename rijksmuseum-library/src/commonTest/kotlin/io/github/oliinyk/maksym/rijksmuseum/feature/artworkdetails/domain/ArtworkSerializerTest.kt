package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain

import arrow.core.NonEmptyList
import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.ArtworkSerializer
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.GettyAatType
import io.github.oliinyk.maksym.rijksmuseum.core.domain.LinguisticObject
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ArtworkSerializerTest {

    @Test
    fun when_serialize_and_deserialize_artwork_then_equal() {
        val artwork = Artwork(
            url = UrlFrom("https://example.com"),
            title = "Title",
            primaryImage = UrlFrom("https://example.com/image.jpg"),
            linguisticObjects = listOf(
                LinguisticObject(
                    type = GettyAatType.Description,
                    descriptions = NonEmptyList.of("Description")
                )
            )
        )

        val json = Json { prettyPrint = true }
        val serialized = json.encodeToString(ArtworkSerializer, artwork)
        val deserialized = json.decodeFromString(ArtworkSerializer, serialized)

        assertEquals(artwork, deserialized)
    }

    @Test
    fun when_serialize_and_deserialize_artwork_with_nulls_then_equal() {
        val artwork = Artwork(
            url = UrlFrom("https://example.com"),
            title = "Title",
            primaryImage = null,
            linguisticObjects = emptyList()
        )

        val json = Json { prettyPrint = true }
        val serialized = json.encodeToString(ArtworkSerializer, artwork)
        val deserialized = json.decodeFromString(ArtworkSerializer, serialized)

        assertEquals(artwork, deserialized)
    }
}
