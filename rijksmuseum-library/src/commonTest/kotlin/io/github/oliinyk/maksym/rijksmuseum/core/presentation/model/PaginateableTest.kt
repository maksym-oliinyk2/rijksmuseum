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
    fun idleList_creates_idle_paginateable_with_provided_data() {
        val paginateable = Paginateable.idleList(items)

        assertEquals(items, paginateable.data)
        assertEquals(Paginateable.Idle, paginateable.state)
    }

    @Test
    fun isRefreshable_depends_on_state_and_data() {
        assertTrue(Paginateable.idleList<String>().isRefreshable)
        assertFalse(Paginateable.idleList(items).isRefreshable)
        assertFalse(Paginateable.loadingList<String>().isRefreshable)
    }

    @Test
    fun isLoadingNext_is_true_when_loading_next_and_false_when_idle() {
        assertTrue(Paginateable.idleList(items).toLoadingNextPage().isLoadingNext)
        assertFalse(Paginateable.idleList(items).isLoadingNext)
    }

    @Test
    fun isIdle_is_true_when_idle_or_exception_and_false_when_loading() {
        assertTrue(Paginateable.idleList(items).isIdle)
        assertTrue(Paginateable.idleList(items).toException(exception).isIdle)
        assertFalse(Paginateable.loadingList<String>().isIdle)
    }

    @Test
    fun canLoadNextForIndex_returns_true_when_idle_hasMore_and_index_at_threshold() {
        val paginateable = Paginateable(data = items, state = Paginateable.Idle, hasMore = true)

        assertTrue(paginateable.canLoadNextForIndex(index = 2, preloadOffset = 1))
        assertFalse(paginateable.canLoadNextForIndex(index = 0, preloadOffset = 1))
    }

    @Test
    fun canLoadNextForIndex_returns_false_when_hasMore_is_false_or_not_idle() {
        assertFalse(Paginateable(data = items, state = Paginateable.Idle, hasMore = false).canLoadNextForIndex(index = 2, preloadOffset = 1))
        assertFalse(Paginateable(data = items, state = Paginateable.Loading, hasMore = true).canLoadNextForIndex(index = 2, preloadOffset = 1))
        assertFalse(Paginateable(data = items, state = Paginateable.LoadingNext, hasMore = true).canLoadNextForIndex(index = 2, preloadOffset = 1))
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
    fun toException_toRefreshing_toLoading_set_correct_state() {
        val withException = Paginateable.idleList(items).toException(exception)
        assertEquals(items, withException.data)
        assertEquals(Paginateable.Exception(exception), withException.state)

        val refreshing = Paginateable.idleList(items).toRefreshing()
        assertEquals(items, refreshing.data)
        assertEquals(Paginateable.Refreshing, refreshing.state)

        val loading = Paginateable.idleList(items).toLoading()
        assertTrue(loading.data.isEmpty())
        assertEquals(Paginateable.Loading, loading.state)
    }

    @Test
    fun toIdle_with_page_replaces_data_from_loading() {
        val newItems = listOf("x", "y")
        val page = Page(data = newItems, hasMore = true)
        val paginateable = Paginateable.loadingList(items).toIdle(page)

        assertEquals(newItems, paginateable.data)
        assertTrue(paginateable.hasMore)
    }
}
