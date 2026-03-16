package io.github.kotlin.fibonacci

public fun generateFibi(): Sequence<Int> = sequence {
    var a = FirstElement
    yield(a)
    var b = SecondElement
    yield(b)
    while (true) {
        val c = a + b
        yield(c)
        a = b
        b = c
    }
}

public expect val FirstElement: Int
public expect val SecondElement: Int
