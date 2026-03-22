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
    fun when_loadingList_then_loading_state_with_empty_data() {
        val paginateable = Paginateable.loadingList<String>()

        assertTrue(paginateable.data.isEmpty())
        assertEquals(Paginateable.Loading, paginateable.state)
    }

    @Test
    fun when_idleList_then_idle_state_with_provided_data() {
        val paginateable = Paginateable.idleList(items)

        assertEquals(items, paginateable.data)
        assertEquals(Paginateable.Idle, paginateable.state)
    }

    @Test
    fun when_idle_empty_then_isRefreshable_true_when_idle_with_data_or_loading_then_false() {
        assertTrue(Paginateable.idleList<String>().isRefreshable)
        assertFalse(Paginateable.idleList(items).isRefreshable)
        assertFalse(Paginateable.loadingList<String>().isRefreshable)
    }

    @Test
    fun when_loadingNext_then_isLoadingNext_true_when_idle_then_false() {
        assertTrue(Paginateable.idleList(items).toLoadingNextPage().isLoadingNext)
        assertFalse(Paginateable.idleList(items).isLoadingNext)
    }

    @Test
    fun when_idle_or_exception_then_isIdle_true_when_loading_then_false() {
        assertTrue(Paginateable.idleList(items).isIdle)
        assertTrue(Paginateable.idleList(items).toException(exception).isIdle)
        assertFalse(Paginateable.loadingList<String>().isIdle)
    }

    @Test
    fun when_idle_hasMore_and_index_at_threshold_then_canLoadNextForIndex_true() {
        val paginateable = Paginateable(data = items, state = Paginateable.Idle, hasMore = true)

        assertTrue(paginateable.canLoadNextForIndex(index = 2, preloadOffset = 1))
        assertFalse(paginateable.canLoadNextForIndex(index = 0, preloadOffset = 1))
    }

    @Test
    fun when_hasMore_false_or_not_idle_then_canLoadNextForIndex_false() {
        assertFalse(
            Paginateable(
                data = items,
                state = Paginateable.Idle,
                hasMore = false
            ).canLoadNextForIndex(index = 2, preloadOffset = 1)
        )
        assertFalse(
            Paginateable(
                data = items,
                state = Paginateable.Loading,
                hasMore = true
            ).canLoadNextForIndex(index = 2, preloadOffset = 1)
        )
        assertFalse(
            Paginateable(
                data = items,
                state = Paginateable.LoadingNext,
                hasMore = true
            ).canLoadNextForIndex(index = 2, preloadOffset = 1)
        )
    }

    @Test
    fun when_toIdle_from_loading_then_data_replaced_and_idle_state() {
        val newItems = listOf("x", "y")
        val paginateable = Paginateable.loadingList(items).toIdle(newItems, hasMore = true)

        assertEquals(newItems, paginateable.data)
        assertEquals(Paginateable.Idle, paginateable.state)
        assertTrue(paginateable.hasMore)
    }

    @Test
    fun when_toIdle_from_loadingNext_then_data_appended_and_idle_state() {
        val newItems = listOf("x", "y")
        val paginateable = Paginateable.idleList(items).toLoadingNextPage().toIdle(newItems, hasMore = false)

        assertEquals(items + newItems, paginateable.data)
        assertEquals(Paginateable.Idle, paginateable.state)
        assertFalse(paginateable.hasMore)
    }

    @Test
    fun when_toException_toRefreshing_toLoading_then_correct_state() {
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
    fun when_toIdle_with_page_then_data_replaced_from_loading() {
        val newItems = listOf("x", "y")
        val page = Page(data = newItems, hasMore = true)
        val paginateable = Paginateable.loadingList(items).toIdle(page)

        assertEquals(newItems, paginateable.data)
        assertTrue(paginateable.hasMore)
    }
}
