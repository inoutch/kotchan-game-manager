package io.github.inoutch.kotchan.game.action.event

import io.github.inoutch.kotchan.game.action.ActionManager.Companion.actionManager
import io.github.inoutch.kotchan.game.action.ActionManager.Companion.eventRunnerContextProvider
import io.github.inoutch.kotchan.game.action.ActionRunner
import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_3
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_5
import io.github.inoutch.kotchan.game.extension.checkClass
import kotlin.reflect.KClass

abstract class EventRunner<T : EventStore, U : Component>(
        eventClass: KClass<T>,
        componentClass: KClass<U>) : ActionRunner {

    val runtimeStore = eventRunnerContextProvider.current.eventRuntimeStore

    val store = runtimeStore.eventStore.checkClass(eventClass)
            ?: throw IllegalStateException(ERR_F_MSG_5(eventClass, runtimeStore.eventStore::class))

    override val id = runtimeStore.id

    val component = componentManager.findById(runtimeStore.componentId, componentClass)
            ?: throw IllegalStateException(ERR_F_MSG_3(componentClass))

    override val componentId = component.raw.id

    val startTime = runtimeStore.startTime

    val endTime = runtimeStore.startTime + runtimeStore.eventStore.durationTime

    var updatable: Boolean = true
        set(value) {
            if (value == field) {
                return
            }
            if (updatable) {
                actionManager.attachUpdatable(this)
            } else {
                actionManager.detachUpdatable(this)
            }
            field = value
        }

    abstract fun update(ratio: Float)

    abstract fun allowInterrupt(): Boolean
}
