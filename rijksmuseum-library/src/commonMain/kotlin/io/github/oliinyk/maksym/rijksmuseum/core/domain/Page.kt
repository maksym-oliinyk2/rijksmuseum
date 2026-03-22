package io.github.oliinyk.maksym.rijksmuseum.core.domain

public data class Page<out T>(
    val data: List<T>,
    val hasMore: Boolean = false,
) {
    internal companion object {
        val End = Page<Any>(emptyList())
    }
}

public data class Paging(
    val currentSize: Int,
    val resultsPerPage: Int = ItemsPerPage
) {
    internal companion object {
        const val ItemsPerPage = 10
        val FirstPage = Paging(currentSize = 0)
    }
}
