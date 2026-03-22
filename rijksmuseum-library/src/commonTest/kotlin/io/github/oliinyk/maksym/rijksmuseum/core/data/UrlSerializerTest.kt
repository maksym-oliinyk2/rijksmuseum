package io.github.oliinyk.maksym.rijksmuseum.core.data

import io.github.oliinyk.maksym.rijksmuseum.core.data.dto.UrlSerializer
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.core.domain.toStringValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class UrlSerializerTest {

    private companion object {
        const val UrlString = """https://www.rijksmuseum.nl"""
    }

    @Serializable
    private data class TestData(
        @Serializable(with = UrlSerializer::class)
        val url: Url
    )

    @Test
    fun when_deserialize_return_valid_url() {
        val input = """{"url": "$UrlString"}"""
        val decoded = Json.decodeFromString<TestData>(input)
        assertEquals(UrlString, decoded.url.toStringValue())
    }
}
