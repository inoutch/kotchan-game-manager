package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.ActionManager.Companion.actionManager
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.action.event.Custom1EventRunnerFactory
import io.github.inoutch.kotchan.game.test.util.action.event.Custom1EventStore
import io.github.inoutch.kotchan.game.test.util.action.task.Custom1TaskRunnerFactory
import io.github.inoutch.kotchan.game.test.util.action.task.Custom1TaskStore
import io.github.inoutch.kotchan.game.test.util.action.task.Custom2TaskRunnerFactory
import io.github.inoutch.kotchan.game.test.util.action.task.Custom2TaskStore
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
    fun checkEventChildren() {
        val componentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        actionManager.registerTaskRunnerFactory(Custom1TaskRunnerFactory())
        actionManager.registerEventRunnerFactory(Custom1EventRunnerFactory())

        var isEnd = false
        val status = mutableListOf<String>()
        actionManager.run(componentId, Custom1TaskStore("ct1", 1)) { isEnd = true }

        actionManager.update(0.499f)
        status.add("ct1-t1-start")
        status.add("ct1-ce1-e1-start")
        assertEquals(status, component.raw.states)

        actionManager.update(0.001f)
        status.add("ct1-ce1-e1-end")
        status.add("ct1-ce2-e1-start")
        assertEquals(status, component.raw.states)

        actionManager.update(1.0f)
        status.add("ct1-ce2-e1-end")
        status.add("ct1-ce3-e1-start")
        status.add("ct1-ce3-e1-end")
        status.add("ct1-t1-end")
        assertEquals(status, component.raw.states)

        actionManager.update(0.1f)
        assertTrue { isEnd }

        // Check cleanup
        assertEquals(0, actionManager.nodeSize)
        assertEquals(0, actionManager.contextSize)
        assertEquals(0, actionManager.runningEventSize)
        assertEquals(0, actionManager.updatingEventSize)
    }

    @Test
    fun checkParallel() {
        val component1Id = componentManager.createComponent(CustomStore("action"))
        assertNotNull(component1Id)

        val component2Id = componentManager.createComponent(CustomStore("action"))
        assertNotNull(component2Id)

        val component1 = componentManager.findById(component1Id, CustomComponent::class)
        assertNotNull(component1)

        val component2 = componentManager.findById(component2Id, CustomComponent::class)
        assertNotNull(component2)

        var isEnd1 = false

        var isEnd2 = false

        val status = mutableListOf<String>()
        actionManager.registerTaskRunnerFactory(Custom1TaskRunnerFactory())
        actionManager.registerEventRunnerFactory(Custom1EventRunnerFactory())

        actionManager.run(component1Id, Custom1TaskStore("ct1")) { isEnd1 = true }
        actionManager.update(0.5f)

        actionManager.run(component2Id, Custom1TaskStore("ct2", 1)) { isEnd2 = true }
        actionManager.update(0.498f)

        status.add("ct2-t1-start")
        status.add("ct2-ce1-e1-start")
        assertEquals(status, component2.raw.states)

        actionManager.update(1.000f)
        status.add("ct2-ce1-e1-end")
        status.add("ct2-ce2-e1-start")
        status.add("ct2-ce2-e1-end")
        status.add("ct2-ce3-e1-start")
        assertEquals(status, component2.raw.states)

        actionManager.update(0.001f)
        status.add("ct2-ce3-e1-update")
        assertEquals(status, component2.raw.states)

        actionManager.update(0.001f)
        status.add("ct2-ce3-e1-end")
        status.add("ct2-t1-end")
        assertEquals(status, component2.raw.states)

        assertFalse { isEnd1 }
        assertTrue { isEnd2 }

        // Check cleanup
        assertEquals(3, actionManager.nodeSize)
        assertEquals(1, actionManager.contextSize)
        assertEquals(1, actionManager.runningEventSize)
        assertEquals(1, actionManager.updatingEventSize)
    }

    @Test
    fun checkTaskChildren() {
        val componentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        actionManager.registerTaskRunnerFactory(Custom1TaskRunnerFactory())
        actionManager.registerTaskRunnerFactory(Custom2TaskRunnerFactory())
        actionManager.registerEventRunnerFactory(Custom1EventRunnerFactory())

        var isEnd = false
        val status = mutableListOf<String>()
        actionManager.run(componentId, Custom2TaskStore("ct2", 1)) { isEnd = true }

        actionManager.update(1.5f)

        status.add("ct2-t1-start")
        status.add("ct2-t1-t1-start")
        status.add("ct2-t1-ce1-e1-start")
        status.add("ct2-t1-ce1-e1-end")
        status.add("ct2-t1-ce2-e1-start")
        status.add("ct2-t1-ce2-e1-end")
        status.add("ct2-t1-ce3-e1-start")
        status.add("ct2-t1-ce3-e1-end")
        status.add("ct2-t1-t1-end")
        status.add("ct2-t2-t1-start")
        assertEquals(status, component.raw.states)

        actionManager.update(0.5f)
        actionManager.update(0.5f)
        actionManager.update(0.5f)

        // EventRunnerの実行はEventRunnerの実行ループで作成された場合は次に持ち越される
        status.add("ct2-t2-ce1-e1-start")

        status.add("ct2-t2-ce1-e1-end")
        status.add("ct2-t2-ce2-e1-start")
        status.add("ct2-t2-ce2-e1-end")
        status.add("ct2-t2-ce3-e1-start")
        status.add("ct2-t2-ce3-e1-end")
        status.add("ct2-t2-t1-end")
        status.add("ct2-t1-end")
        assertEquals(status, component.raw.states)
        assertTrue { isEnd }

        // Check cleanup
        assertEquals(0, actionManager.nodeSize)
        assertEquals(0, actionManager.contextSize)
        assertEquals(0, actionManager.runningEventSize)
        assertEquals(0, actionManager.updatingEventSize)
    }

    @Test
    fun checkInterrupt() {

    }
}
