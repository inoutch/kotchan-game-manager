package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.event.EventManager.Companion.eventManager
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import io.github.inoutch.kotchan.game.test.util.event.Custom1EventFactor
import io.github.inoutch.kotchan.game.test.util.event.Custom1EventCreator
import io.github.inoutch.kotchan.game.test.util.event.Custom1EventCreatorRunnerFactory
import io.github.inoutch.kotchan.game.test.util.event.Custom1EventFactorRunnerFactory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EventManagerTest {
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
    }

    @Test
    fun update() {
        val componentId = componentManager.createComponent(CustomStore("test"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        eventManager.enqueue(componentId, Custom1EventCreator())

        eventManager.update(0.0f)

        assertEquals("event-update", component.raw.state)

        eventManager.update(0.499f)

        assertEquals("event-update", component.raw.state)

        eventManager.update(0.001f)

        assertEquals("event-end", component.raw.state)

        eventManager.update(0.001f)

        assertEquals("event-update", component.raw.state)
    }
}
