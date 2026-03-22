package io.github.oliinyk.maksym.rijksmuseum.core.presentation.model

import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.exception_unknown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoadableTest {

    private val data = "test data"
    private val exception = AppException(Res.string.exception_unknown)

    @Test
    fun idleSingle_creates_idle_loadable_with_data() {
        val loadable = Loadable.idleSingle(data)

        assertEquals(data, loadable.data)
        assertEquals(Loadable.Idle, loadable.state)
    }

    @Test
    fun loadingSingle_without_data_creates_loading_loadable_with_null() {
        val loadable = Loadable.loadingSingle<String>()

        assertNull(loadable.data)
        assertEquals(Loadable.Loading, loadable.state)
    }

    @Test
    fun loadingSingle_with_data_creates_loading_loadable() {
        val loadable = Loadable.loadingSingle(data)

        assertEquals(data, loadable.data)
        assertEquals(Loadable.Loading, loadable.state)
    }

    @Test
    fun loadingList_creates_loading_loadable_with_empty_list() {
        val loadable = Loadable.loadingList<String>()

        assertTrue(loadable.data.isEmpty())
        assertEquals(Loadable.Loading, loadable.state)
    }

    @Test
    fun idleList_creates_idle_loadable_with_data() {
        val items = listOf("a", "b")
        val loadable = Loadable.idleList(items)

        assertEquals(items, loadable.data)
        assertEquals(Loadable.Idle, loadable.state)
    }

    @Test
    fun idleList_creates_idle_loadable_with_empty_list_by_default() {
        val loadable = Loadable.idleList<String>()

        assertTrue(loadable.data.isEmpty())
        assertEquals(Loadable.Idle, loadable.state)
    }

    @Test
    fun isRefreshable_is_true_when_idle() {
        val loadable = Loadable.idleSingle(data)

        assertTrue(loadable.isRefreshable)
    }

    @Test
    fun isRefreshable_is_false_when_loading() {
        val loadable = Loadable.loadingSingle(data)

        assertFalse(loadable.isRefreshable)
    }

    @Test
    fun isRefreshable_is_false_when_refreshing() {
        val loadable = Loadable.idleSingle(data).toRefreshing()

        assertFalse(loadable.isRefreshable)
    }

    @Test
    fun isLoading_is_true_when_loading() {
        val loadable = Loadable.loadingSingle(data)

        assertTrue(loadable.isLoading)
    }

    @Test
    fun isLoading_is_false_when_idle() {
        val loadable = Loadable.idleSingle(data)

        assertFalse(loadable.isLoading)
    }

    @Test
    fun isRefreshing_is_true_when_refreshing() {
        val loadable = Loadable.idleSingle(data).toRefreshing()

        assertTrue(loadable.isRefreshing)
    }

    @Test
    fun isRefreshing_is_false_when_idle() {
        val loadable = Loadable.idleSingle(data)

        assertFalse(loadable.isRefreshing)
    }

    @Test
    fun isIdle_is_true_when_idle() {
        val loadable = Loadable.idleSingle(data)

        assertTrue(loadable.isIdle)
    }

    @Test
    fun isIdle_is_true_when_exception() {
        val loadable = Loadable.idleSingle(data).toException(exception)

        assertTrue(loadable.isIdle)
    }

    @Test
    fun isIdle_is_false_when_loading() {
        val loadable = Loadable.loadingSingle(data)

        assertFalse(loadable.isIdle)
    }

    @Test
    fun isException_is_true_when_exception() {
        val loadable = Loadable.idleSingle(data).toException(exception)

        assertTrue(loadable.isException)
    }

    @Test
    fun isException_is_false_when_idle() {
        val loadable = Loadable.idleSingle(data)

        assertFalse(loadable.isException)
    }

    @Test
    fun toIdle_updates_data_and_sets_idle_state() {
        val newData = "new data"
        val loadable = Loadable.loadingSingle(data).toIdle(newData)

        assertEquals(newData, loadable.data)
        assertEquals(Loadable.Idle, loadable.state)
    }

    @Test
    fun toException_sets_exception_state_and_preserves_data() {
        val loadable = Loadable.idleSingle(data).toException(exception)

        assertEquals(data, loadable.data)
        assertEquals(Loadable.Exception(exception), loadable.state)
    }

    @Test
    fun toRefreshing_sets_refreshing_state_and_preserves_data() {
        val loadable = Loadable.idleSingle(data).toRefreshing()

        assertEquals(data, loadable.data)
        assertEquals(Loadable.Refreshing, loadable.state)
    }

    @Test
    fun toLoading_sets_loading_state_and_clears_data() {
        val loadable = Loadable.idleSingle<String?>(data).toLoading()

        assertNull(loadable.data)
        assertEquals(Loadable.Loading, loadable.state)
    }
}
