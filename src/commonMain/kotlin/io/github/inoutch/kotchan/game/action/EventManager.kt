package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.context.EventRunnerContext
import io.github.inoutch.kotchan.game.action.factory.EventRunnerFactory
import io.github.inoutch.kotchan.game.action.runner.EventRunner
import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_2
import io.github.inoutch.kotchan.game.extension.className
import io.github.inoutch.kotchan.game.extension.fastForEach
import io.github.inoutch.kotchan.game.util.ContextProvider
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal
import kotlinx.serialization.Serializable

class EventManager : TaskManager.EventListener {
    @ThreadLocal
    companion object {
        val eventRunnerContextProvider = ContextProvider<EventRunnerContext>()
    }

    @Serializable
    class Store(val events: List<EventRuntimeStore>) {
        companion object {
            fun create(): Store {
                return Store(emptyList())
            }
        }
    }

    private val factories = mutableMapOf<String, EventRunnerFactory>()

    private val eventQueue = mutableMapOf<String, MutableList<EventRunner<*, *>>>()

    private val eventsSortedByStartTime = mutableListOf<EventRunner<*, *>>()

    private val eventsSortedByEndTime = mutableListOf<EventRunner<*, *>>()

    private val updatableEvents = mutableListOf<EventRunner<*, *>>()

    private var time = 0L

    private val listeners = mutableMapOf<String, MutableList<EventManagerListener>>()

    fun store(): Store {
        return Store((eventsSortedByStartTime + eventsSortedByEndTime).map { it.runtimeStore })
    }

    fun restore(store: Store) {
        eventQueue.clear()
        eventsSortedByStartTime.clear()
        eventsSortedByEndTime.clear()
        updatableEvents.clear()

        store.events.fastForEach { enqueue(it) }
        startEventRunners(true)
    }

    override fun enqueue(eventRuntimeStore: EventRuntimeStore) {
        val currentEvents = eventQueue
                .getOrPut(eventRuntimeStore.componentId) { mutableListOf() }

        val factory = factories[eventRuntimeStore.eventStore.factoryClass]
        checkNotNull(factory) { ERR_F_MSG_2(eventRuntimeStore.eventStore.factoryClass, factory) }

        val runner = eventRunnerContextProvider.run(EventRunnerContext(eventRuntimeStore)) {
            factory.create()
        }
        currentEvents.add(runner)
        eventsSortedByStartTime.add(runner)
    }

    override fun interrupt(componentId: String) {
        val currentEvents = eventQueue.getValue(componentId)
        currentEvents.first().interrupt()
        currentEvents.clear()
    }

    fun update(currentTime: Long) {
        time = currentTime

        var updateOnce = true
        do {
            var running = endEventRunners()

            if (updateOnce) {
                updateEventRunners()
                updateOnce = false
            }

            running += startEventRunners()
        } while (running > 0)
    }

    fun registerFactory(factory: EventRunnerFactory) {
        factories[className(factory::class)] = factory
    }

    fun unregisterFactories() {
        factories.clear()
    }

    fun registerListener(componentId: String, listener: EventManagerListener) {
        listeners.getOrPut(componentId) { mutableListOf() }.add(listener)
    }

    fun unregisterListener(componentId: String, listener: EventManagerListener) {
        listeners[componentId]?.remove(listener)
    }

    private fun endEventRunners(): Int {
        val eventRunners = pullEndingEvents()
        if (eventRunners.isEmpty()) {
            return 0
        }

        for (eventRunner in eventRunners) {
            // EventRunnerの終了
            eventRunner.end()
            listeners[eventRunner.componentId]?.fastForEach { it.end(eventRunner.runtimeStore) }

            if (eventRunner.updatable) {
                updatableEvents.remove(eventRunner)
            }
            eventQueue.getValue(eventRunner.componentId).remove(eventRunner)
        }
        return eventRunners.size
    }

    private fun startEventRunners(skipOlderEventStarting: Boolean = false): Int {
        val eventRunners = pullStartingEvents()
        if (eventRunners.isEmpty()) {
            return 0
        }

        for (eventRunner in eventRunners) {
            val exists = eventQueue.getValue(eventRunner.componentId)

            if (!skipOlderEventStarting || time == eventRunner.startTime) {
                eventRunner.start()
                listeners[eventRunner.componentId]?.fastForEach { it.start(eventRunner.runtimeStore) }
            }

            if (eventRunner.endTime <= time && exists.size >= 2) {
                eventRunner.end()
                listeners[eventRunner.componentId]?.fastForEach { it.end(eventRunner.runtimeStore) }
                exists.remove(eventRunner)
                continue
            }

            if (eventRunner.updatable) {
                updatableEvents.add(eventRunner)
            }
            eventsSortedByEndTime.add(eventRunner)
        }
        return eventRunners.size
    }

    private fun updateEventRunners() {
        updatableEvents.fastForEach { it.update(ratio(it.runtimeStore)) }
    }

    private fun pullStartingEvents(): List<EventRunner<*, *>> {
        var index = 0
        val buffers = mutableListOf<EventRunner<*, *>>()

        while (index < eventsSortedByStartTime.size) {
            val buffer = eventsSortedByStartTime[index++]
            if (buffer.startTime > time) {
                break
            }
            buffers.add(buffer)
        }

        for (i in 0 until buffers.size) {
            eventsSortedByStartTime.removeAt(0)
        }
        return buffers
    }

    private fun pullEndingEvents(): List<EventRunner<*, *>> {
        var skipped = 0
        val buffers = arrayListOf<EventRunner<*, *>>()

        while (skipped < eventsSortedByEndTime.size) {
            val buffer = eventsSortedByEndTime[skipped]
            if (buffer.endTime > time) {
                break
            }
            val exists = eventQueue.getValue(buffer.componentId)
            if (exists.size < 2) {
                skipped++
            } else {
                eventsSortedByEndTime.removeAt(skipped)
                buffers.add(buffer)
            }
        }
        return buffers
    }

    private fun ratio(eventRuntimeStore: EventRuntimeStore): Float {
        return min(((time - eventRuntimeStore.startTime).toFloat() / eventRuntimeStore.eventStore.durationTime), 1.0f)
    }
}
