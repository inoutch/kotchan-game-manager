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

    enum class Mode {
        Server,
        Client,
    }

    class EventCreatorContext(
            var runningEventFactors: Int = 0,
            var endTime: Long = 0,
            // All events queue
            val eventStoreQueue: MutableList<EventStore> = mutableListOf())

    val eventCreatorSize: Int
        get() = eventCreators.values.size

    val eventFactorSize: Int
        get() = eventsSortedByEndTime.size

    private val eventCreators = mutableMapOf<String, EventCreatorContext>()

    private val eventCreatorsToStart = arrayListOf<EventCreator<*, *>>()

    private val eventsSortedByStartTime = arrayListOf<EventReducer<*, *>>()

    private val eventsSortedByEndTime = arrayListOf<EventReducer<*, *>>()

    private val updateEventRunners = mutableListOf<EventReducer<*, *>>()

    // <KClass string, factory>
    private val eventFactorFactories = mutableMapOf<String, EventReducerFactory>()

    private val eventCreatorFactories = mutableMapOf<String, EventCreatorFactory>()

    private val idManager = IdManager()

    private var time = 0L

    // Serialization
    private lateinit var serializer: ProtoBuf

    private var eventTransfer: EventTransfer? = null

    private var mode: Mode = Mode.Server

    fun init(registerCallback: SerializersModuleBuilder.() -> Unit) {
        val module = SerializersModule(registerCallback)
        serializer = ProtoBuf(context = module)

        eventCreators.clear()
        eventCreatorsToStart.clear()
        eventsSortedByStartTime.clear()
        eventsSortedByEndTime.clear()
        updateEventRunners.clear()

        unregisterAllEventFactorRunnerFactories()
        unregisterAllEventCreatorRunnerFactories()
    }

    //---- For common
    fun registerEventFactorRunnerFactory(eventReducerFactory: EventReducerFactory) {
        eventFactorFactories[className(eventReducerFactory::class)] = eventReducerFactory
    }

    fun registerEventCreatorRunnerFactory(eventCreatorFactory: EventCreatorFactory) {
        eventCreatorFactories[className(eventCreatorFactory::class)] = eventCreatorFactory
    }

    fun registerEventTransfer(eventTransfer: EventTransfer) {
        this.eventTransfer = eventTransfer
    }

    fun unregisterAllEventFactorRunnerFactories() {
        eventFactorFactories.clear()
    }

    fun unregisterAllEventCreatorRunnerFactories() {
        eventCreatorFactories.clear()
    }

    fun update(delta: Float) {
        time += (delta * 1000.0f).toLong()

        endEventReducers()

        updateEvents()

        startEventCreators()

        startEventReducers()
    }

    fun dump(eventRuntime: EventRuntime): ByteArray {
        return serializer.dump(EventRuntime.serializer(), eventRuntime)
    }

    fun load(bytes: ByteArray): EventRuntime {
        return serializer.load(EventRuntime.serializer(), bytes)
    }

    fun attachUpdatable(eventReducer: EventReducer<*, *>) {
        if (eventReducer.isEnded) {
            // Ignore
            return
        }
        updateEventRunners.add(eventReducer)
    }

    fun detachUpdatable(eventReducer: EventReducer<*, *>) {
        if (eventReducer.isEnded) {
            // Ignore
            return
        }
        updateEventRunners.remove(eventReducer)
    }

    private fun ratio(eventRuntime: EventRuntime): Float {
        if (eventRuntime.eventStore !is EventReducerStore) {
            return 0.0f
        }
        return min(((time - eventRuntime.startTime).toDouble() / eventRuntime.eventStore.durationTime).toFloat(), 1.0f)
    }

    private fun updateEvents() {
        updateEventRunners.fastForEach { it.update(ratio(it.eventRuntime)) }
    }

    //---- For server
    // Server only
    fun enqueue(componentId: String, eventStore: EventCreatorStore) {
        val context = eventCreators.getOrPut(componentId) { EventCreatorContext() }
        context.eventStoreQueue.add(eventStore)

        if (context.runningEventFactors == 0) {
            eventCreatorsToStart.add(createEventCreator(componentId, eventStore))
        }
    }

    private fun createEventCreator(componentId: String, eventStore: EventCreatorStore): EventCreator<*, *> {
        val factory = eventCreatorFactories[eventStore.factoryClass]
        checkNotNull(factory) { ERR_F_MSG_4(eventStore.factoryClass, factory) }

        return contextProvider.run(EventRuntime(idManager.nextId(), componentId, eventStore, time)) { factory.create() }
    }

    private fun startEventReducer(componentId: String, event: EventReducerStore) {
        val factory = eventFactorFactories[event.factoryClass]
        checkNotNull(factory) { ERR_F_MSG_2(event.factoryClass, factory) }

        val context = eventCreators.getOrPut(componentId) { EventCreatorContext() }
        if (context.endTime < time) {
            context.endTime = time
        }

        val eventRuntime = contextProvider.run(EventRuntime(
                idManager.nextId(),
                componentId,
                event,
                context.endTime)) { factory.create() }

        eventsSortedByStartTime.add(eventRuntime)
        context.endTime += event.durationTime
        context.runningEventFactors += 1
    }

    @Suppress("UNUSED_PARAMETER")
    private fun startEventReducerEnd(componentId: String, event: EventReducerStoreEnd) {
        val context = eventCreators.getValue(componentId)
        context.eventStoreQueue.removeAt(0)

        startEventCreator(componentId, context)
    }

    private fun startEventReducers() {
        if (mode != Mode.Server) {
            return
        }

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

    private fun endEventReducers() {
        if (mode != Mode.Server) {
            return
        }

        val events = pullEndingEvents()
        if (events.isEmpty()) {
            return
        }

        for (x in events) {
            val componentId = x.component.raw.id
            val context = eventCreators.getValue(componentId)
            context.runningEventFactors -= 1
            context.eventStoreQueue.removeAt(0)

            // Notify end event factor
            x.end()
            x.isEnded = true
            if (x.updatable) {
                updateEventRunners.remove(x)
            }

            if (context.runningEventFactors != 0) {
                continue
            }

            // If event factors is not queued, queue event to start
            val event = context.eventStoreQueue.firstOrNull()
            if (event == null) {
                eventCreators.remove(componentId)
                continue
            }

            check(event is EventCreatorStore) { ERR_V_MSG_7 }
            eventCreatorsToStart.add(createEventCreator(componentId, event))
        }
    }

    private fun startEventCreators() {
        if (mode != Mode.Server) {
            return
        }

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

            if (builder.eventStoreQueue.isEmpty()) {
                // No enqueue more events
                startEventReducerEnd(componentId, EventReducerStoreEnd())
                continue
            }

            // Add events on the head
            context.eventStoreQueue.addAll(0, builder.eventStoreQueue)
            startEventCreator(componentId, context)
        }
    }

    private fun startEventCreator(componentId: String, context: EventCreatorContext) {
        when (val first = context.eventStoreQueue.firstOrNull()) {
            null -> {
                eventCreators.remove(componentId)
            }
            is EventCreatorStore -> {
                eventCreatorsToStart.add(createEventCreator(componentId, first))
            }
            is EventReducerStore -> {
                // Else if first event is event factor, append event factors until an event creator
                for (x in context.eventStoreQueue) {
                    if (x is EventReducerStore) {
                        startEventReducer(componentId, x)
                    } else if (x is EventCreatorStore) {
                        break
                    } else throw IllegalStateException(ERR_V_MSG_6)
                }
            }
        }
    }

    private fun pullStartingEvents(): List<EventReducer<*, *>> {
        var index = 0
        val buffers = mutableListOf<EventReducer<*, *>>()

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

    private fun pullEndingEvents(): List<EventReducer<*, *>> {
        var index = 0
        val buffers = arrayListOf<EventReducer<*, *>>()

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

    //---- For client
    fun receive(eventRuntime: EventRuntime) {
        if (mode != Mode.Client) {
            return
        }

        val context = eventCreators.getOrPut(eventRuntime.componentId) { EventCreatorContext() }
        context.eventStoreQueue.add(eventRuntime.eventStore)
    }
}
