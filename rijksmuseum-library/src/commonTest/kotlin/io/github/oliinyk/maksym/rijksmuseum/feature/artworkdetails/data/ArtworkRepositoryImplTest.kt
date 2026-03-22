package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.data

import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.data.TestRijksmuseumApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArtworkRepositoryImplTest {

    private val testUrl = UrlFrom("https://example.com/1")
    private val testArtwork = Artwork(
        url = testUrl,
        title = "Artwork 1",
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        linguisticObjects = listOf()
    )

    @Test
    fun when_fetch_then_api_call() = runTest {
        val api = TestRijksmuseumApi(
            artworksDetails = mapOf(testUrl to testArtwork.right())
        )
        val repository = ArtworkRepositoryImpl(api)

        val result = repository.fetchArtworkDetails(testUrl)

        assertEquals(testArtwork.right(), result)
    }
}
