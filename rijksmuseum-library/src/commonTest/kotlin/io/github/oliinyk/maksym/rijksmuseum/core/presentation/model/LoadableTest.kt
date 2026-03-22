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
    fun when_idleSingle_then_idle_state_with_data() {
        val loadable = Loadable.idleSingle(data)

        assertEquals(data, loadable.data)
        assertEquals(Loadable.Idle, loadable.state)
    }

    @Test
    fun when_loadingSingle_without_data_then_loading_state_with_null() {
        val loadable = Loadable.loadingSingle<String>()

        assertNull(loadable.data)
        assertEquals(Loadable.Loading, loadable.state)
    }

    @Test
    fun when_loadingList_then_loading_state_with_empty_list() {
        val loadable = Loadable.loadingList<String>()

        assertTrue(loadable.data.isEmpty())
        assertEquals(Loadable.Loading, loadable.state)
    }

    @Test
    fun when_idle_then_isRefreshable_true_when_loading_or_refreshing_then_false() {
        assertTrue(Loadable.idleSingle(data).isRefreshable)
        assertFalse(Loadable.loadingSingle(data).isRefreshable)
        assertFalse(Loadable.idleSingle(data).toRefreshing().isRefreshable)
    }

    @Test
    fun when_loading_then_isLoading_true_when_idle_then_false() {
        assertTrue(Loadable.loadingSingle(data).isLoading)
        assertFalse(Loadable.idleSingle(data).isLoading)
    }

    @Test
    fun when_refreshing_then_isRefreshing_true_when_idle_then_false() {
        assertTrue(Loadable.idleSingle(data).toRefreshing().isRefreshing)
        assertFalse(Loadable.idleSingle(data).isRefreshing)
    }

    @Test
    fun when_idle_or_exception_then_isIdle_true_when_loading_then_false() {
        assertTrue(Loadable.idleSingle(data).isIdle)
        assertTrue(Loadable.idleSingle(data).toException(exception).isIdle)
        assertFalse(Loadable.loadingSingle(data).isIdle)
    }

    @Test
    fun when_exception_then_isException_true_when_idle_then_false() {
        assertTrue(Loadable.idleSingle(data).toException(exception).isException)
        assertFalse(Loadable.idleSingle(data).isException)
    }

    @Test
    fun when_toIdle_then_data_updated_and_idle_state() {
        val newData = "new data"
        val loadable = Loadable.loadingSingle(data).toIdle(newData)

        assertEquals(newData, loadable.data)
        assertEquals(Loadable.Idle, loadable.state)
    }

    @Test
    fun when_toException_then_exception_state_and_data_preserved() {
        val loadable = Loadable.idleSingle(data).toException(exception)

        assertEquals(data, loadable.data)
        assertEquals(Loadable.Exception(exception), loadable.state)
    }

    @Test
    fun when_toRefreshing_then_refreshing_state_when_toLoading_then_loading_state() {
        val refreshing = Loadable.idleSingle(data).toRefreshing()
        assertEquals(data, refreshing.data)
        assertEquals(Loadable.Refreshing, refreshing.state)

        val loading = Loadable.idleSingle<String?>(data).toLoading()
        assertNull(loading.data)
        assertEquals(Loadable.Loading, loading.state)
    }
}
