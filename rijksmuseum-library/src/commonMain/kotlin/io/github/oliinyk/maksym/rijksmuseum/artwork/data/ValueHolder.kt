package io.github.oliinyk.maksym.rijksmuseum.artwork.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class ValueHolder<V>(initial: V? = null) {
    private var value = initial
    private val mutex = Mutex()

    suspend fun get(): V? = mutex.withLock { value }
    suspend fun getAndForget(): V? = mutex.withLock {
        val temp = value
        value = null
        temp
    }

    suspend fun set(newValue: V) = mutex.withLock { value = newValue }
    suspend fun clear() = mutex.withLock { value = null }

}
