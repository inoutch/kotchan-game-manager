package io.github.inoutch.kotchan.game.error

import io.github.inoutch.kotchan.game.component.ComponentFactory
import io.github.inoutch.kotchan.game.extension.className
import kotlin.reflect.KClass

const val ERR_V_MSG_0 = "Parent may be not created yet. Do not access parent before CREATE"
const val ERR_V_MSG_1 = "Parent id is null"
const val ERR_V_MSG_2 = "Invalid parent id"
const val ERR_V_MSG_4 = "Activated callback is not registered"
const val ERR_V_MSG_5 = "Factory type is null"

fun ERR_F_MSG_0(expected: KClass<*>, actual: KClass<*>) =
        "Invalid access of store [expected: ${className(expected)}, actual: ${className(actual)}]"

fun ERR_F_MSG_1(expected: String, actual: ComponentFactory?) =
        "ComponentFactory is not registered [expected: $expected, actual: ${actual?.let { className(it::class) }}]"
