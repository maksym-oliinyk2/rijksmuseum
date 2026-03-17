package io.github.oliinyk.maksym.rijksmuseum.artworks

public data class Page<out T>(
    val data: List<T>,
    val hasMore: Boolean = false,
) {
    internal companion object {
        val End = Page<Any>(emptyList())
    }
}

/*public inline val Page<*>.hasMore: Boolean
    get() = next != null*/

public data class Paging(
    val currentSize: Int,
    val resultsPerPage: Int = ItemsPerPage
) {
    internal companion object {
        const val ItemsPerPage = 20
        val FirstPage = Paging(currentSize = 0)
    }
}
