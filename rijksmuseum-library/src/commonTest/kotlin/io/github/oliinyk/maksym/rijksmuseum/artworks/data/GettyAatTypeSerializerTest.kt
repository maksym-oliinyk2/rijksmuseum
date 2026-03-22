package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GettyAatTypeSerializerTest {

    @Serializable
    private data class TestData(
        @Serializable(with = GettyAatTypeSerializer::class)
        val type: GettyAatType?
    )

    @Test
    fun when_deserialize_known_id_then_success() {
        val input = """{"type": "http://vocab.getty.edu/aat/300435414"}"""
        val decoded = Json.decodeFromString<TestData>(input)
        assertEquals(GettyAatType.Inscription, decoded.type)
    }

    @Test
    fun when_deserialize_unknown_id_then_null() {
        val input = """{"type": "http://vocab.getty.edu/aat/unknown"}"""
        val decoded = Json.decodeFromString<TestData>(input)
        assertNull(decoded.type)
    }

    @Test
    fun when_serialize_known_type_then_success() {
        val input = TestData(GettyAatType.Notes)
        val encoded = Json.encodeToString<TestData>(input)
        assertEquals("""{"type":"http://vocab.getty.edu/aat/300435452"}""", encoded)
    }

    @Test
    fun when_serialize_null_type_then_success() {
        val input = TestData(null)
        val encoded = Json.encodeToString<TestData>(input)
        assertEquals("""{"type":null}""", encoded)
    }
}
