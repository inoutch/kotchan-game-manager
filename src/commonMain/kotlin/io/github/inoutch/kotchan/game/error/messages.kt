package io.github.inoutch.kotchan.game.error

import io.github.inoutch.kotchan.game.action.factory.EventRunnerFactory
import io.github.inoutch.kotchan.game.action.factory.TaskRunnerFactory
import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentFactory
// import io.github.inoutch.kotchan.game.action.task.TaskRunnerFactory
// import io.github.inoutch.kotchan.game.action.event.EventRunnerFactory
import io.github.inoutch.kotchan.game.extension.className
import kotlin.reflect.KClass

const val ERR_V_MSG_0 = "Parent may be not created yet. Do not access parent before CREATE."
const val ERR_V_MSG_1 = "Component is already registered."
const val ERR_V_MSG_2 = "Invalid parent id."
const val ERR_V_MSG_4 = "Activated callback is not registered."
const val ERR_V_MSG_5 = "Factory type is null."
const val ERR_V_MSG_6 = "Component is not registered."
const val ERR_V_MSG_7 = "Invalid action was received"
const val ERR_V_MSG_8 = "ActionNode is not registered by id"
const val ERR_V_MSG_9 = "ActionComponentContext is not registered by component id"
const val ERR_V_MSG_10 = "There are not running action of its component id"

fun ERR_F_MSG_0(expected: KClass<*>, actual: KClass<*>) =
        "Invalid access of store [expected: ${className(expected)}, actual: ${className(actual)}]"

fun ERR_F_MSG_1(expected: String, actual: ComponentFactory?) =
        "ComponentFactory is not registered [expected: $expected, actual: ${actual?.let { className(it::class) }}]"

fun ERR_F_MSG_2(expected: String, actual: EventRunnerFactory?) =
        "EventRunnerFactory is not registered [expected: $expected, actual: ${actual?.let { className(it::class) }}]"

fun <T : Component> ERR_F_MSG_3(expected: KClass<T>) =
        "Not found or invalid component [expected: $expected]"

fun ERR_F_MSG_4(expected: String, actual: TaskRunnerFactory?) =
        "TaskRunnerFactory is not registered [expected: $expected, actual: ${actual?.let { className(it::class) }}]"

fun ERR_F_MSG_5(expected: KClass<*>, actual: KClass<*>) =
        "Invalid access of actionStore [expected: ${className(expected)}, actual: ${className(actual)}]"
