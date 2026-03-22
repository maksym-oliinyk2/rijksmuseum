package io.github.oliinyk.maksym.rijksmuseum.core.presentation.model

import androidx.compose.runtime.Immutable
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Page
import kotlin.jvm.JvmInline

@Immutable
public data class Paginateable<out T>(
    val data: List<T>,
    val state: State,
    val hasMore: Boolean = false,
) {
    public companion object {

        public fun <T> loadingList(
            data: List<T> = listOf(),
        ): Paginateable<T> = Paginateable(data = data, state = Loading)

        public fun <T> idleList(
            data: List<T> = listOf(),
        ): Paginateable<T> = Paginateable(data = data, state = Idle)
    }

    @Immutable
    public sealed interface State

    @JvmInline
    public value class Exception(
        public val exception: AppException,
    ) : State

    public data object Loading : State

    public data object LoadingNext : State

    public data object Refreshing : State

    public data object Idle : State
}

public val Paginateable<*>.isRefreshable: Boolean
    get() = isIdle && data.isEmpty()

public fun Paginateable<*>.canLoadNextForIndex(
    index: Int,
    preloadOffset: Int,
): Boolean = isIdle && hasMore && index >= data.lastIndex - preloadOffset

public val Paginateable<*>.isLoading: Boolean
    get() = state === Paginateable.Loading

public val Paginateable<*>.isLoadingNext: Boolean
    get() = state === Paginateable.LoadingNext

public val Paginateable<*>.isRefreshing: Boolean
    get() = state === Paginateable.Refreshing

public val Paginateable<*>.isIdle: Boolean
    get() = state === Paginateable.Idle || isException

public val Paginateable<*>.isException: Boolean
    get() = state is Paginateable.Exception

internal fun <T> Paginateable<T>.toIdle(
    data: List<T>,
    hasMore: Boolean,
): Paginateable<T> =
    when (state) {
        Paginateable.LoadingNext, is Paginateable.Exception -> copy(
            data = this.data + data,
            state = Paginateable.Idle,
            hasMore = hasMore
        )

        Paginateable.Loading, Paginateable.Refreshing, Paginateable.Idle -> copy(
            data = data,
            state = Paginateable.Idle,
            hasMore = hasMore
        )
    }

internal fun <T> Paginateable<T>.toIdle(
    page: Page<T>,
): Paginateable<T> = toIdle(page.data, page.hasMore)

internal fun <T> Paginateable<T>.toException(
    cause: AppException,
): Paginateable<T> = copy(state = Paginateable.Exception(cause))

internal fun <T : Any> Paginateable<T>.toLoadingNextPage(): Paginateable<T> {
    require(data.isNotEmpty()) { "data should be loaded first" }
    return copy(state = Paginateable.LoadingNext)
}

internal fun <T : Any> Paginateable<T>.toLoading(): Paginateable<T> =
    // reset data on (re)load
    copy(state = Paginateable.Loading, data = listOf())

internal fun <T : Any> Paginateable<T>.toRefreshing(): Paginateable<T> =
    copy(state = Paginateable.Refreshing)
