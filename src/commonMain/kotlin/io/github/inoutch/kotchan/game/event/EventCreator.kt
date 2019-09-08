package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_3
import io.github.inoutch.kotchan.game.event.EventManager.Companion.contextProvider
import kotlin.reflect.KClass

abstract class EventCreator<T : EventStore, U : Component>(
        val eventClass: KClass<T>,
        val componentClass: KClass<U>) : Event {

    val eventRuntime = contextProvider.current

    val eventFactory = eventRuntime.eventStore as EventCreatorStore

    val component = componentManager.findById(eventRuntime.componentId, componentClass)
            ?: throw IllegalStateException(ERR_F_MSG_3(componentClass))

    abstract fun next(builder: EventBuilder)
}
