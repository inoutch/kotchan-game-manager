package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.context.EventRunnerContext
import io.github.inoutch.kotchan.game.action.factory.EventRunnerFactory
import io.github.inoutch.kotchan.game.action.runner.EventRunner
import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_2
import io.github.inoutch.kotchan.game.extension.className
import io.github.inoutch.kotchan.game.util.ContextProvider

class EventManager : TaskManager.Action {
    companion object {
        val eventRunnerContextProvider = ContextProvider<EventRunnerContext>()
    }

    private val factories = mutableMapOf<String, EventRunnerFactory>()

    // シリアライズ対象
    private val runningEvents = mutableMapOf<String, MutableList<EventRunner<*, *>>>()

    fun enqueue(eventRuntimeStore: EventRuntimeStore) {
        val currentEvents = runningEvents
                .getOrPut(eventRuntimeStore.componentId) { mutableListOf() }

        val factory = factories[eventRuntimeStore.eventStore.factoryClass]
        checkNotNull(factory) { ERR_F_MSG_2(eventRuntimeStore.eventStore.factoryClass, factory) }

        currentEvents.add(factory.create())
    }

    fun interrupt(componentId: String) {
        val currentEvents = runningEvents.getValue(componentId)
        currentEvents.first().interrupt()
        currentEvents.clear()
    }

    fun run(currentTime: Long) {
        for (events in runningEvents.values) {
            while (events.size >= 2 && currentTime < events.first().endTime) {
                // イベントが2つ以上で終了時刻を過ぎている場合にイベントを終了させる
                events.first().end()
                events.removeAt(0)
            }
        }
    }

    fun registerFactory(factory: EventRunnerFactory) {
        factories[className(factory::class)] = factory
    }

    fun unregisterFactories() {
        factories.clear()
    }

    override fun checkInterruptsAllowed(componentId: String): Boolean {
        return runningEvents.getValue(componentId).first().allowInterrupt()
    }
}
