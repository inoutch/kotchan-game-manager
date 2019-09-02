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

class EventManagerTest {
    private lateinit var component: CustomComponent

    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())

        eventManager.init {
            polymorphic(Event::class) {
                Custom1EventFactor::class with Custom1EventFactor.serializer()
            }
        }
        eventManager.registerEventCreatorRunnerFactory(Custom1EventCreatorRunnerFactory())
        eventManager.registerEventFactorRunnerFactory(Custom1EventFactorRunnerFactory())
        eventManager.registerEventCreatorRunnerFactory(Custom2EventCreatorRunnerFactory())

        val componentId = componentManager.createComponent(CustomStore("test"))
                ?: throw IllegalStateException("Could not create componentId")

        component = componentManager.findById(componentId, CustomComponent::class)?.raw
                ?: throw IllegalStateException("Could not create component")
    }

    @Test
    fun createEventCreator() {
        val expectStates = mutableListOf("event-start")

        eventManager.enqueue(component.id, Custom1EventCreator())

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

        eventManager.enqueue(component.id, Custom2EventCreator("test2"))

        eventManager.update(0.0f)
        assertEquals(expectStates, component.states)

        eventManager.update(0.099f)
        expectStates.add("event-update")
        assertEquals(expectStates, component.states)
    }
}
