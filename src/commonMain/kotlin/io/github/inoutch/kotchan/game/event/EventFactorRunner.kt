package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_3
import io.github.inoutch.kotchan.game.error.ERR_V_MSG_4
import io.github.inoutch.kotchan.game.event.EventManager.Companion.contextProvider
import io.github.inoutch.kotchan.game.event.EventManager.Companion.eventManager
import kotlin.reflect.KClass

abstract class EventFactorRunner<T : Event, U : Component>(
        val eventClass: KClass<T>,
        val componentClass: KClass<U>) : EventRunner {

    val eventRuntime = contextProvider.current

    val eventFactor = eventRuntime.event as EventFactor

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
