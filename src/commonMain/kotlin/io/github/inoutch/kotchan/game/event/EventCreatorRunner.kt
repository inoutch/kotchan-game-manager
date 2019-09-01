package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.event.EventManager.Companion.contextProvider
import kotlin.reflect.KClass

abstract class EventCreatorRunner<T : Event, U : Component>(
        val eventClass: KClass<T>,
        val componentClass: KClass<U>) : EventRunner {

    val eventRuntime = contextProvider.current

    val eventFactory = eventRuntime.event as EventCreator

    abstract fun next(builder: EventBuilder)
}
