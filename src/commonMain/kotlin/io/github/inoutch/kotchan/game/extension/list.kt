package io.github.inoutch.kotchan.game.extension

import kotlin.reflect.KClass

fun <T, U : KClass<T>> List<*>.filterIsInstance(kClass: U): List<T> {
    val results = mutableListOf<T>()
    this.forEach { x -> x?.checkClass(kClass)?.let { y -> results.add(y) } }
    return results
}

fun <T, U> List<T>.findAndReturn(predicate: (value: T) -> U?): U? {
    for (x in this) {
        return predicate(x) ?: continue
    }
    return null
}

fun <T> List<T>.combine(): List<Pair<T, T>> {
    val pairs = mutableListOf<Pair<T, T>>()
    for (x in 0 until this.size) {
        for (y in x + 1 until this.size) {
            pairs.add(Pair(this[x], this[y]))
        }
    }
    return pairs
}
