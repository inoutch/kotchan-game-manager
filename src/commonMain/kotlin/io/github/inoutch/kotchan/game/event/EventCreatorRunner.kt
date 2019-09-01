package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_3
import io.github.inoutch.kotchan.game.error.ERR_V_MSG_6
import io.github.inoutch.kotchan.game.event.EventManager.Companion.contextProvider
import kotlin.reflect.KClass

abstract class EventCreatorRunner<T : Event, U : Component>(
        val eventClass: KClass<T>,
        val componentClass: KClass<U>) : EventRunner {

    val eventRuntime = contextProvider.current

    val eventFactory = eventRuntime.event as EventCreator

    val component = componentManager.findById(eventRuntime.componentId, componentClass)
            ?: throw IllegalStateException(ERR_F_MSG_3(componentClass))

    abstract fun next(builder: EventBuilder)
}
