package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_3
import io.github.inoutch.kotchan.game.event.EventManager.Companion.contextProvider
import io.github.inoutch.kotchan.game.event.EventManager.Companion.eventManager
import kotlin.reflect.KClass

abstract class EventReducer<T : EventStore, U : Component>(
        val eventClass: KClass<T>,
        val componentClass: KClass<U>) : Event {

    val eventRuntime = contextProvider.current

    val eventFactor = eventRuntime.eventStore as EventReducerStore

    val component = componentManager.findById(eventRuntime.componentId, componentClass)
            ?: throw IllegalStateException(ERR_F_MSG_3(componentClass))

    val startTime = eventRuntime.startTime

    val endTime = eventRuntime.startTime + eventFactor.durationTime

    var updatable: Boolean = true
        set(value) {
            if (value == field) {
                return
            }
            if (updatable) {
                eventManager.attachUpdatable(this)
            } else {
                eventManager.detachUpdatable(this)
            }
            field = value
        }

    var isEnded = false

    abstract fun start()

    abstract fun update(ratio: Float)

    abstract fun end()
}
