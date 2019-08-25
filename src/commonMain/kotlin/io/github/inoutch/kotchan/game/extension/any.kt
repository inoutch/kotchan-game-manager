package io.github.inoutch.kotchan.game.extension

import kotlin.reflect.KClass

fun <T : KClass<U>, U> Any.checkClass(kClass: T): U? {
    if (kClass.isInstance(this)) {
        @Suppress("UNCHECKED_CAST")
        return this as U
    }
    return null
}
