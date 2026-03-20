package io.github.oliinyk.maksym.rijksmuseum.artwork.data

// this is not a ***thread safe*** wrapper over a generic value
internal class ValueHolder<V>(
    var value: V? = null,
)

internal fun <V> ValueHolder<V>.getAndForget(): V? {
    val temp = value
    value = null
    return temp
}
