package io.github.oliinyk.maksym.rijksmuseum.core.domain

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

/**
 * Represents data that can be loaded from remote source
 *
 * @param data data to be loaded
 * @param state loading state
 */
@Immutable
public data class Loadable<out T>(
    val data: T,
    val state: State,
) {
    public companion object {

        public fun <T> idleSingle(
            data: T,
        ): Loadable<T> = Loadable(data = data, state = Idle)

        public fun <T> loadingSingle(): Loadable<T?> = Loadable(data = null, state = Loading)

        public fun <T> loadingSingle(
            data: T,
        ): Loadable<T> = Loadable(data = data, state = Loading)

        public fun <T : Any> loadingList(): Loadable<List<T>> =
            Loadable(data = listOf(), state = Loading)

        public fun <T : Any> idleList(
            data: List<T> = listOf(),
        ): Loadable<List<T>> = Loadable(data = data, state = Idle)
    }

    @Immutable
    public sealed interface State

    @JvmInline
    public value class Exception(
        public val exception: AppException,
    ) : State

    public data object Loading : State

    public data object Refreshing : State

    public data object Idle : State
}

public val Loadable<*>.isRefreshable: Boolean
    get() = isIdle

public val Loadable<*>.isLoading: Boolean
    get() = state === Loadable.Loading

public val Loadable<*>.isRefreshing: Boolean
    get() = state === Loadable.Refreshing

public val Loadable<*>.isIdle: Boolean
    get() = state === Loadable.Idle || isException

public val Loadable<*>.isException: Boolean
    get() = state is Loadable.Exception

internal fun <T> Loadable<T>.toIdle(
    data: T,
): Loadable<T> = copy(data = data, state = Loadable.Idle)

internal fun <T> Loadable<T>.toException(
    cause: AppException,
): Loadable<T> = copy(state = Loadable.Exception(cause))

internal fun <T> Loadable<T?>.toLoading(): Loadable<T?> =
    copy(data = null, state = Loadable.Loading)

internal fun <T> Loadable<T>.toRefreshing(): Loadable<T> =
    copy(state = Loadable.Refreshing)
