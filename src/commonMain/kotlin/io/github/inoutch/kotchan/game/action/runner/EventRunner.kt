package io.github.inoutch.kotchan.game.action.runner

import io.github.inoutch.kotchan.game.action.EventManager.Companion.eventRunnerContextProvider
import io.github.inoutch.kotchan.game.action.store.EventStore
import io.github.inoutch.kotchan.game.component.Component
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

    override val componentId = runtimeStore.componentId

    val startTime = runtimeStore.startTime

    val endTime = runtimeStore.startTime + runtimeStore.eventStore.durationTime

    abstract fun start()

    abstract fun end()

    abstract fun update(ratio: Float)

    abstract fun allowInterrupt(): Boolean // 即時割り込み可能であればtrue
}
