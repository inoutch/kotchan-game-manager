package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.ActionManager.Companion.actionManager
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.action.event.Custom1EventRunnerFactory
import io.github.inoutch.kotchan.game.test.util.action.event.Custom1EventStore
import io.github.inoutch.kotchan.game.test.util.action.task.Custom1TaskRunnerFactory
import io.github.inoutch.kotchan.game.test.util.action.task.Custom1TaskStore
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import kotlin.test.*

class ActionManagerTest {
    @BeforeTest
    fun before() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())

        actionManager.init {
            polymorphic(ActionStore::class) {
                Custom1TaskStore::class with Custom1TaskStore.serializer()
                Custom1EventStore::class with Custom1EventStore.serializer()
            }
        }
    }

    @Test
    fun standard() {
        val componentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        actionManager.registerTaskRunnerFactory(Custom1TaskRunnerFactory())
        actionManager.registerEventRunnerFactory(Custom1EventRunnerFactory())

        var isEnd = false
        val status = mutableListOf<String>()
        actionManager.run(componentId, Custom1TaskStore("custom1")) { isEnd = true }

        actionManager.update(0.499f)
        status.add("start-e1")
        assertEquals(status, component.raw.states)

        actionManager.update(0.001f)
        status.add("end-e1")
        status.add("start-e2")
        assertEquals(status, component.raw.states)

        actionManager.update(1.0f)
        status.add("end-e2")
        status.add("start-e3")
        status.add("end-e3")
        assertEquals(status, component.raw.states)

        assertFalse { isEnd }
    }

    @Test
    fun parallel() {
        val component1Id = componentManager.createComponent(CustomStore("action"))
        assertNotNull(component1Id)

        val component2Id = componentManager.createComponent(CustomStore("action"))
        assertNotNull(component2Id)

        val component1 = componentManager.findById(component1Id, CustomComponent::class)
        assertNotNull(component1)

        val component2 = componentManager.findById(component2Id, CustomComponent::class)
        assertNotNull(component2)

        val status = mutableListOf<String>()
        actionManager.registerTaskRunnerFactory(Custom1TaskRunnerFactory())
        actionManager.registerEventRunnerFactory(Custom1EventRunnerFactory())

        actionManager.run(component1Id, Custom1TaskStore("custom1"))
        actionManager.update(0.5f)

        actionManager.run(component2Id, Custom1TaskStore("custom2"))
        actionManager.update(0.499f)

        status.add("start-e1")
        assertEquals(status, component2.raw.states)
    }
}
