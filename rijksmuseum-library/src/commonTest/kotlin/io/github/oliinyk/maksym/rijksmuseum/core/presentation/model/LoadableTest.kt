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
    fun loadingList_creates_loading_loadable_with_empty_list() {
        val loadable = Loadable.loadingList<String>()

        assertTrue(loadable.data.isEmpty())
        assertEquals(Loadable.Loading, loadable.state)
    }

    @Test
    fun isRefreshable_is_true_when_idle_and_false_when_loading_or_refreshing() {
        assertTrue(Loadable.idleSingle(data).isRefreshable)
        assertFalse(Loadable.loadingSingle(data).isRefreshable)
        assertFalse(Loadable.idleSingle(data).toRefreshing().isRefreshable)
    }

    @Test
    fun isLoading_is_true_when_loading_and_false_when_idle() {
        assertTrue(Loadable.loadingSingle(data).isLoading)
        assertFalse(Loadable.idleSingle(data).isLoading)
    }

    @Test
    fun isRefreshing_is_true_when_refreshing_and_false_when_idle() {
        assertTrue(Loadable.idleSingle(data).toRefreshing().isRefreshing)
        assertFalse(Loadable.idleSingle(data).isRefreshing)
    }

    @Test
    fun isIdle_is_true_when_idle_or_exception_and_false_when_loading() {
        assertTrue(Loadable.idleSingle(data).isIdle)
        assertTrue(Loadable.idleSingle(data).toException(exception).isIdle)
        assertFalse(Loadable.loadingSingle(data).isIdle)
    }

    @Test
    fun isException_is_true_when_exception_and_false_when_idle() {
        assertTrue(Loadable.idleSingle(data).toException(exception).isException)
        assertFalse(Loadable.idleSingle(data).isException)
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
    fun toRefreshing_and_toLoading_set_correct_state() {
        val refreshing = Loadable.idleSingle(data).toRefreshing()
        assertEquals(data, refreshing.data)
        assertEquals(Loadable.Refreshing, refreshing.state)

        val loading = Loadable.idleSingle<String?>(data).toLoading()
        assertNull(loading.data)
        assertEquals(Loadable.Loading, loading.state)
    }
}
