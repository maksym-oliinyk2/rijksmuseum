package io.github.oliinyk.maksym.rijksmuseum.core.domain

/**
 * Represents a single page of results returned from a paginated data source.
 *
 * @param T The type of items contained in this page.
 * @property data The list of items on this page.
 * @property hasMore `true` if there are more pages available after this one, `false` if this is the last page.
 */
public data class Page<out T>(
    val data: List<T>,
    val hasMore: Boolean = false,
) {
    internal companion object {
        val End = Page<Any>(emptyList())
    }
}

/**
 * Tracks the current pagination state for a paginated request.
 *
 * @property currentSize The total number of items already loaded across all pages so far.
 * @property resultsPerPage The number of items requested per page. Defaults to [Paging.Companion.ItemsPerPage].
 */
public data class Paging(
    val currentSize: Int,
    val resultsPerPage: Int = ItemsPerPage
) {
    internal companion object {
        const val ItemsPerPage = 10
        val FirstPage = Paging(currentSize = 0)
    }
}
