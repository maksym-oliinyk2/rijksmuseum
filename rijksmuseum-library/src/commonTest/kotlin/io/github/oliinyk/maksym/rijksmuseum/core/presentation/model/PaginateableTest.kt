package io.github.oliinyk.maksym.rijksmuseum.core.presentation.model

import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Page
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.exception_unknown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaginateableTest {

    private val items = listOf("a", "b", "c")
    private val exception = AppException(Res.string.exception_unknown)

    @Test
    fun loadingList_creates_loading_paginateable_with_empty_data() {
        val paginateable = Paginateable.loadingList<String>()

        assertTrue(paginateable.data.isEmpty())
        assertEquals(Paginateable.Loading, paginateable.state)
    }

    @Test
    fun loadingList_creates_loading_paginateable_with_provided_data() {
        val paginateable = Paginateable.loadingList(items)

        assertEquals(items, paginateable.data)
        assertEquals(Paginateable.Loading, paginateable.state)
    }

    @Test
    fun idleList_creates_idle_paginateable_with_empty_data_by_default() {
        val paginateable = Paginateable.idleList<String>()

        assertTrue(paginateable.data.isEmpty())
        assertEquals(Paginateable.Idle, paginateable.state)
    }

    @Test
    fun idleList_creates_idle_paginateable_with_provided_data() {
        val paginateable = Paginateable.idleList(items)

        assertEquals(items, paginateable.data)
        assertEquals(Paginateable.Idle, paginateable.state)
    }

    @Test
    fun isRefreshable_is_true_when_idle_and_data_empty() {
        val paginateable = Paginateable.idleList<String>()

        assertTrue(paginateable.isRefreshable)
    }

    @Test
    fun isRefreshable_is_false_when_idle_but_data_not_empty() {
        val paginateable = Paginateable.idleList(items)

        assertFalse(paginateable.isRefreshable)
    }

    @Test
    fun isRefreshable_is_false_when_loading() {
        val paginateable = Paginateable.loadingList<String>()

        assertFalse(paginateable.isRefreshable)
    }

    @Test
    fun isLoading_is_true_when_loading() {
        val paginateable = Paginateable.loadingList<String>()

        assertTrue(paginateable.isLoading)
    }

    @Test
    fun isLoading_is_false_when_idle() {
        val paginateable = Paginateable.idleList<String>()

        assertFalse(paginateable.isLoading)
    }

    @Test
    fun isLoadingNext_is_true_when_loading_next() {
        val paginateable = Paginateable.idleList(items).toLoadingNextPage()

        assertTrue(paginateable.isLoadingNext)
    }

    @Test
    fun isLoadingNext_is_false_when_idle() {
        val paginateable = Paginateable.idleList(items)

        assertFalse(paginateable.isLoadingNext)
    }

    @Test
    fun isRefreshing_is_true_when_refreshing() {
        val paginateable = Paginateable.idleList(items).toRefreshing()

        assertTrue(paginateable.isRefreshing)
    }

    @Test
    fun isRefreshing_is_false_when_idle() {
        val paginateable = Paginateable.idleList(items)

        assertFalse(paginateable.isRefreshing)
    }

    @Test
    fun isIdle_is_true_when_idle() {
        val paginateable = Paginateable.idleList(items)

        assertTrue(paginateable.isIdle)
    }

    @Test
    fun isIdle_is_true_when_exception() {
        val paginateable = Paginateable.idleList(items).toException(exception)

        assertTrue(paginateable.isIdle)
    }

    @Test
    fun isIdle_is_false_when_loading() {
        val paginateable = Paginateable.loadingList<String>()

        assertFalse(paginateable.isIdle)
    }

    @Test
    fun isException_is_true_when_exception() {
        val paginateable = Paginateable.idleList(items).toException(exception)

        assertTrue(paginateable.isException)
    }

    @Test
    fun isException_is_false_when_idle() {
        val paginateable = Paginateable.idleList(items)

        assertFalse(paginateable.isException)
    }

    @Test
    fun canLoadNextForIndex_returns_true_when_idle_hasMore_and_index_at_threshold() {
        val paginateable = Paginateable(data = items, state = Paginateable.Idle, hasMore = true)

        assertTrue(paginateable.canLoadNextForIndex(index = 2, preloadOffset = 1))
    }

    @Test
    fun canLoadNextForIndex_returns_true_when_index_beyond_threshold() {
        val paginateable = Paginateable(data = items, state = Paginateable.Idle, hasMore = true)

        assertTrue(paginateable.canLoadNextForIndex(index = 2, preloadOffset = 0))
    }

    @Test
    fun canLoadNextForIndex_returns_false_when_index_before_threshold() {
        val paginateable = Paginateable(data = items, state = Paginateable.Idle, hasMore = true)

        assertFalse(paginateable.canLoadNextForIndex(index = 0, preloadOffset = 1))
    }

    @Test
    fun canLoadNextForIndex_returns_false_when_hasMore_is_false() {
        val paginateable = Paginateable(data = items, state = Paginateable.Idle, hasMore = false)

        assertFalse(paginateable.canLoadNextForIndex(index = 2, preloadOffset = 1))
    }

    @Test
    fun canLoadNextForIndex_returns_false_when_loading() {
        val paginateable = Paginateable(data = items, state = Paginateable.Loading, hasMore = true)

        assertFalse(paginateable.canLoadNextForIndex(index = 2, preloadOffset = 1))
    }

    @Test
    fun canLoadNextForIndex_returns_false_when_loading_next() {
        val paginateable = Paginateable(data = items, state = Paginateable.LoadingNext, hasMore = true)

        assertFalse(paginateable.canLoadNextForIndex(index = 2, preloadOffset = 1))
    }

    @Test
    fun toIdle_from_loading_replaces_data_and_sets_idle() {
        val newItems = listOf("x", "y")
        val paginateable = Paginateable.loadingList(items).toIdle(newItems, hasMore = true)

        assertEquals(newItems, paginateable.data)
        assertEquals(Paginateable.Idle, paginateable.state)
        assertTrue(paginateable.hasMore)
    }

    @Test
    fun toIdle_from_loading_next_appends_data_and_sets_idle() {
        val newItems = listOf("x", "y")
        val paginateable = Paginateable.idleList(items).toLoadingNextPage().toIdle(newItems, hasMore = false)

        assertEquals(items + newItems, paginateable.data)
        assertEquals(Paginateable.Idle, paginateable.state)
        assertFalse(paginateable.hasMore)
    }

    @Test
    fun toIdle_with_page_replaces_data_from_loading() {
        val newItems = listOf("x", "y")
        val page = Page(data = newItems, hasMore = true)
        val paginateable = Paginateable.loadingList(items).toIdle(page)

        assertEquals(newItems, paginateable.data)
        assertTrue(paginateable.hasMore)
    }

    @Test
    fun toException_sets_exception_state_and_preserves_data() {
        val paginateable = Paginateable.idleList(items).toException(exception)

        assertEquals(items, paginateable.data)
        assertEquals(Paginateable.Exception(exception), paginateable.state)
    }

    @Test
    fun toLoadingNextPage_sets_loading_next_state_and_preserves_data() {
        val paginateable = Paginateable.idleList(items).toLoadingNextPage()

        assertEquals(items, paginateable.data)
        assertEquals(Paginateable.LoadingNext, paginateable.state)
    }

    @Test
    fun toLoading_sets_loading_state_and_clears_data() {
        val paginateable = Paginateable.idleList(items).toLoading()

        assertTrue(paginateable.data.isEmpty())
        assertEquals(Paginateable.Loading, paginateable.state)
    }

    @Test
    fun toRefreshing_sets_refreshing_state_and_preserves_data() {
        val paginateable = Paginateable.idleList(items).toRefreshing()

        assertEquals(items, paginateable.data)
        assertEquals(Paginateable.Refreshing, paginateable.state)
    }
}
