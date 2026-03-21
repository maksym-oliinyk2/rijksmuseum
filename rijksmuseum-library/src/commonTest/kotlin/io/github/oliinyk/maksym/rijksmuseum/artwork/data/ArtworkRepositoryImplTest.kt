package io.github.oliinyk.maksym.rijksmuseum.artwork.data

import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.list.TestRijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArtworkRepositoryImplTest {

    private val testUrl = UrlFrom("https://example.com/1")
    private val testArtwork = Artwork(
        url = testUrl,
        title = Title("Artwork 1"),
        primaryImage = UrlFrom("https://example.com/1.jpg"),
        descriptions = listOf()
    )

    @Test
    fun when_fetch_cache_empty_then_api_call() = runTest {
        val api = TestRijksmuseumApi(
            artworksDetails = mapOf(testUrl to testArtwork.right())
        )
        val cache = ValueHolder<Artwork>()
        val repository = ArtworkRepositoryImpl(api, cache, StandardTestDispatcher(testScheduler))

        val result = repository.fetchArtworkDetails(testUrl)

        assertEquals(testArtwork.right(), result)
    }

    @Test
    fun when_fetch_cache_hit_then_cached_data() = runTest {
        val api = TestRijksmuseumApi(
            artworksDetails = emptyMap()
        )
        val cache = ValueHolder(testArtwork)
        val repository = ArtworkRepositoryImpl(api, cache, StandardTestDispatcher(testScheduler))

        val result = repository.fetchArtworkDetails(testUrl)

        assertEquals(testArtwork.right(), result)
        assertEquals(null, cache.value)
    }
}
