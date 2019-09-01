package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.error.*
import io.github.inoutch.kotchan.game.extension.className
import io.github.inoutch.kotchan.game.extension.fastForEach
import io.github.inoutch.kotchan.game.util.ContextProvider
import io.github.inoutch.kotchan.game.util.IdManager
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal

class EventManager {
    @ThreadLocal
    companion object {
        val eventManager = EventManager()

        val contextProvider = ContextProvider<EventRuntime>()
    }

    class EventFactoryContext(
            var runningEventFactors: Int,
            var endTime: Long,
            val eventQueue: MutableList<Event>) {
        constructor() : this(0, 0, mutableListOf())
    }

    private val eventCreators = mutableMapOf<String, EventFactoryContext>()

    private val eventCreatorsToStart = arrayListOf<EventCreatorRunner<*, *>>()

    private val eventsSortedByStartTime = arrayListOf<EventFactorRunner<*, *>>()

    private val eventsSortedByEndTime = arrayListOf<EventFactorRunner<*, *>>()

    private val updateEventRunners = mutableListOf<EventFactorRunner<*, *>>()

    // <KClass string, factory>
    private val eventFactorFactories = mutableMapOf<String, EventFactorRunnerFactory>()

    private val eventCreatorFactories = mutableMapOf<String, EventCreatorRunnerFactory>()

    private val idManager = IdManager()

    private var time = 0L

    // Serialization
    private lateinit var serializer: ProtoBuf

    fun init(registerCallback: SerializersModuleBuilder.() -> Unit) {
        val module = SerializersModule(registerCallback)
        serializer = ProtoBuf(context = module)

        unregisterAllEventFactorRunnerFactories()
        unregisterAllEventCreatorRunnerFactories()
    }

    fun enqueue(componentId: String, event: EventFactor) {
        val factory = eventFactorFactories[event.factoryClass]
        checkNotNull(factory) { ERR_F_MSG_2(event.factoryClass, factory) }

        val context = eventCreators.getOrPut(componentId) { EventFactoryContext() }
        if (context.endTime < time) {
            context.endTime = time
        }
        eventsSortedByStartTime.add(contextProvider.run(EventRuntime(idManager.nextId(), componentId, event, context.endTime)) {
            factory.create()
        })
        context.endTime += event.durationTime
        context.runningEventFactors += 1
    }

    fun enqueue(componentId: String, event: EventCreator) {
        val context = eventCreators.getOrPut(componentId) { EventFactoryContext() }
        context.eventQueue.add(event)

        if (context.runningEventFactors == 0) {
            eventCreatorsToStart.add(createEventCreatorRunner(componentId, event))
        }
    }

    fun enqueue(componentId: String, event: EventFactorEnd) {
        val context = eventCreators.getValue(componentId)
        context.eventQueue.removeAt(0)

        if (context.eventQueue.isEmpty()) {
            // Clean up
            eventCreators.remove(componentId)
            return
        }
    }

    fun registerEventFactorRunnerFactory(eventFactorRunnerFactory: EventFactorRunnerFactory) {
        eventFactorFactories[className(eventFactorRunnerFactory::class)] = eventFactorRunnerFactory
    }

    fun registerEventCreatorRunnerFactory(eventCreatorRunnerFactory: EventCreatorRunnerFactory) {
        eventCreatorFactories[className(eventCreatorRunnerFactory::class)] = eventCreatorRunnerFactory
    }

    fun unregisterAllEventFactorRunnerFactories() {
        eventFactorFactories.clear()
    }

    fun unregisterAllEventCreatorRunnerFactories() {
        eventCreatorFactories.clear()
    }

    fun update(delta: Float) {
        time += (delta * 1000.0f).toLong()

        cleanUpEvents()
        processEvents()
        updateEvents()
        executeEventCreators()
    }

    fun dump(eventRuntime: EventRuntime): ByteArray {
        return serializer.dump(EventRuntime.serializer(), eventRuntime)
    }

    fun load(bytes: ByteArray): EventRuntime {
        return serializer.load(EventRuntime.serializer(), bytes)
    }

    fun attachUpdatable(eventFactorRunner: EventFactorRunner<*, *>) {
        if (eventFactorRunner.isEnded) {
            // Ignore
            return
        }
        updateEventRunners.add(eventFactorRunner)
    }

    fun detachUpdatable(eventFactorRunner: EventFactorRunner<*, *>) {
        if (eventFactorRunner.isEnded) {
            // Ignore
            return
        }
        updateEventRunners.remove(eventFactorRunner)
    }

    private fun createEventCreatorRunner(componentId: String, event: EventCreator): EventCreatorRunner<*, *> {
        val factory = eventCreatorFactories[event.factoryClass]
        checkNotNull(factory) { ERR_F_MSG_4(event.factoryClass, factory) }

        return contextProvider.run(EventRuntime(idManager.nextId(), componentId, event, time)) { factory.create() }
    }

    private fun ratio(eventRuntime: EventRuntime): Float {
        if (eventRuntime.event !is EventFactor) {
            return 0.0f
        }
        return min(((time - eventRuntime.startTime).toDouble() / eventRuntime.event.durationTime).toFloat(), 1.0f)
    }

    private fun updateEvents() {
        updateEventRunners.fastForEach { it.update(ratio(it.eventRuntime)) }
    }

    private fun processEvents() {
        val events = pullStartingEvents()
        if (events.isEmpty()) {
            return
        }

        for (eventRunner in events) {
            eventRunner.start()

            if (eventRunner.endTime <= time) {
                eventRunner.end()
                continue
            }

            eventsSortedByEndTime.add(eventRunner)
            if (eventRunner.updatable) {
                updateEventRunners.add(eventRunner)
            }
        }
        eventsSortedByEndTime.sortBy { it.endTime }
    }

    private fun cleanUpEvents() {
        val events = pullEndingEvents()
        if (events.isEmpty()) {
            return
        }

        for (x in events) {
            val componentId = x.component.raw.id
            val context = eventCreators.getValue(componentId)
            context.runningEventFactors -= 1

            // If event factors is not queued, queue event to start
            if (context.runningEventFactors == 0) {
                val event = context.eventQueue.firstOrNull()
                if (event != null) {
                    when (event) {
                        is EventFactor -> {
                            enqueue(componentId, event)
                        }
                        is EventCreator -> {
                            eventCreatorsToStart.add(createEventCreatorRunner(componentId, event))
                        }
                    }
                    // Remove event factor from queue
                    context.eventQueue.removeAt(0)
                } else {
                    eventCreators.remove(componentId)
                }
            }
            // Notify end event factor
            x.end()
            x.isEnded = true
            if (x.updatable) {
                updateEventRunners.remove(x)
            }
        }
    }

    private fun executeEventCreators() {
        // DO NOT PROCESSING BY CLIENT
        while (eventCreatorsToStart.isNotEmpty()) {
            // Pick up event creator runner
            val eventCreatorRunner = eventCreatorsToStart.first()
            val componentId = eventCreatorRunner.eventRuntime.componentId
            val context = eventCreators.getValue(componentId)

            eventCreatorsToStart.removeAt(0)

            // Execute event creator runner
            val builder = EventBuilder()
            eventCreatorRunner.next(builder)

            if (builder.eventQueue.isEmpty()) {
                // No enqueue more events
                enqueue(componentId, EventFactorEnd())
                continue
            }

            // Add events on the head
            context.eventQueue.addAll(0, builder.eventQueue)

            val first = builder.eventQueue.first()
            if (first is EventCreator) {
                // If first event is event create, append event creator to start
                eventCreatorsToStart.add(createEventCreatorRunner(componentId, first))
            } else if (first is EventFactor) {
                // Else if first event is event factor, append event factors until an event creator
                for (x in builder.eventQueue) {
                    if (x is EventFactor) {
                        enqueue(componentId, x)
                    } else if (x is EventCreator) {
                        break
                    } else throw IllegalStateException(ERR_V_MSG_6)
                }
            }
        }
    }

    private fun pullStartingEvents(): List<EventFactorRunner<*, *>> {
        var index = 0
        val buffers = mutableListOf<EventFactorRunner<*, *>>()

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

    private fun pullEndingEvents(): List<EventFactorRunner<*, *>> {
        var index = 0
        val buffers = arrayListOf<EventFactorRunner<*, *>>()

        while (index < eventsSortedByEndTime.size) {
            val buffer = eventsSortedByEndTime[index++]
            if (buffer.eventRuntime.endTime > time) {
                break
            }
            buffers.add(buffer)
        }

        for (x in buffers) {
            eventsSortedByEndTime.removeAt(0)
        }
        return buffers
    }
}
