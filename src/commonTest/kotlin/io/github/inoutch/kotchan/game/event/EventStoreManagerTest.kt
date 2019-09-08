package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.event.EventManager.Companion.eventManager
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import io.github.inoutch.kotchan.game.test.util.event.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventStoreManagerTest {
    private lateinit var component: CustomComponent

    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())

        eventManager.init {
            polymorphic(EventStore::class) {
                Custom1EventReducerStore::class with Custom1EventReducerStore.serializer()
            }
        }
        eventManager.registerEventCreatorRunnerFactory(Custom1EventCreatorFactory())
        eventManager.registerEventFactorRunnerFactory(Custom1EventReducerFactory())
        eventManager.registerEventCreatorRunnerFactory(Custom2EventCreatorFactory())

        val componentId = componentManager.createComponent(CustomStore("test"))
                ?: throw IllegalStateException("Could not create componentId")

        component = componentManager.findById(componentId, CustomComponent::class)?.raw
                ?: throw IllegalStateException("Could not create component")
    }

    @Test
    fun createEventCreator() {
        val expectStates = mutableListOf("event-start")

        eventManager.enqueue(component.id, Custom1EventCreatorStore())

        eventManager.update(0.0f)

        assertEquals(expectStates, component.states)

        eventManager.update(0.499f)

        expectStates.add("event-update")
        assertEquals(expectStates, component.states)

        eventManager.update(0.001f)

        expectStates.addAll(listOf("event-end", "event-start"))
        assertEquals(expectStates, component.states)

        eventManager.update(0.001f)

        expectStates.addAll(listOf("event-update"))
        assertEquals(expectStates, component.states)

        // Result
        assertEquals(1, eventManager.eventCreatorSize)
        assertEquals(1, eventManager.eventFactorSize)
    }

    @Test
    fun childEventCreator() {
        val expectStates = mutableListOf("event-start")

        eventManager.enqueue(component.id, Custom2EventCreatorStore("test2"))

        eventManager.update(0.0f)
        assertEquals(expectStates, component.states)

        eventManager.update(0.099f)
        expectStates.add("event-update")
        assertEquals(expectStates, component.states)

        eventManager.update(0.001f)
        expectStates.addAll(listOf("event-end", "event-start"))
        assertEquals(expectStates, component.states)

        eventManager.update(0.099f)
        expectStates.add("event-update")
        assertEquals(expectStates, component.states)

        eventManager.update(0.001f)
        expectStates.addAll(listOf("event-end", "event-start"))
        assertEquals(expectStates, component.states)

        eventManager.update(0.099f)
        expectStates.add("event-update")
        assertEquals(expectStates, component.states)

        eventManager.update(0.001f)
        expectStates.addAll(listOf("event-end"))
        assertEquals(expectStates, component.states)

        eventManager.update(0.001f)
        assertEquals(expectStates, component.states)

        // Result
        assertEquals(0, eventManager.eventCreatorSize)
        assertEquals(0, eventManager.eventFactorSize)
    }
}
