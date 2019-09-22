package io.github.inoutch.kotchan.game.action.event

import io.github.inoutch.kotchan.game.action.ActionManager.Companion.eventRunnerContextProvider
import io.github.inoutch.kotchan.game.action.ActionRunner
import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_3
import kotlin.reflect.KClass

abstract class EventRunner<T : EventStore, U : Component>(
        val eventClass: KClass<T>,
        val componentClass: KClass<U>) : ActionRunner {

    val runtimeStore = eventRunnerContextProvider.current.eventRuntimeStore

    val store = runtimeStore.eventStore

    override val id = runtimeStore.id

    val component = componentManager.findById(runtimeStore.componentId, componentClass)
            ?: throw IllegalStateException(ERR_F_MSG_3(componentClass))

    override val componentId = component.raw.id

    val endTime = runtimeStore.startTime + runtimeStore.eventStore.durationTime

//    var updatable: Boolean = true
//        set(value) {
//            if (value == field) {
//                return
//            }
//            if (updatable) {
//                eventManager.attachUpdatable(this)
//            } else {
//                eventManager.detachUpdatable(this)
//            }
//            field = value
//        }

    abstract fun start()

    abstract fun update(ratio: Float)

    abstract fun end()
}
