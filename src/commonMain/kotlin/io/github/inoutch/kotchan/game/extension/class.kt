package io.github.inoutch.kotchan.game.extension

import kotlin.reflect.KClass

fun className(kClass: KClass<*>): String {
    return kClass::qualifiedName.get()!!
}
