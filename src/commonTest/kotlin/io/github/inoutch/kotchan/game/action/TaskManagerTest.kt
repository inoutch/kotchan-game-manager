package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.custom.Custom1EventRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom1TaskRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom1TaskStore
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import kotlin.test.*

class TaskManagerTest {
    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())
    }

    @Test
    fun checkStandard() {
        val componentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        val eventManager = EventManager()
        val taskManager = TaskManager(eventManager)
        taskManager.addTaskListener(eventManager)

        eventManager.registerFactory(Custom1EventRunnerFactory())
        taskManager.registerFactory(Custom1TaskRunnerFactory())

        var isEnded = false
        val history = mutableListOf<String>()
        taskManager.registerComponent(componentId, object : TaskManager.ComponentListener {
            override fun onEnd() {
                isEnded = true
            }
        })

        taskManager.run(componentId, Custom1TaskStore("root", 2, 2))

        taskManager.update(0.4f)
        eventManager.update(taskManager.currentTime)

        history.add("root:e1:s")
        assertEquals(history, component.raw.history) // 0.4 [s]

        taskManager.update(0.1f)
        eventManager.update(taskManager.currentTime)

        history.add("root:e1:e")
        history.add("root:e2:s")
        assertEquals(history, component.raw.history) // 0.5 [s,e,s]

        taskManager.update(0.6f)
        eventManager.update(taskManager.currentTime)

        history.add("root:e2:e")
        history.add("root-t1:e1:s")
        assertEquals(history, component.raw.history) // 1.1 [s,e,s,e,s]

        taskManager.update(0.4f)
        eventManager.update(taskManager.currentTime)

        history.add("root-t1:e1:e")
        history.add("root-t1:e2:s")
        assertEquals(history, component.raw.history) // 1.5 [s,e,s,e,s,e,s]

        assertFalse { isEnded }
    }

    @Test
    fun checkIsEnded() {
        val componentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        val eventManager = EventManager()
        val taskManager = TaskManager(eventManager)
        taskManager.addTaskListener(eventManager)

        eventManager.registerFactory(Custom1EventRunnerFactory())
        taskManager.registerFactory(Custom1TaskRunnerFactory())

        var isEnded = false
        taskManager.registerComponent(componentId, object : TaskManager.ComponentListener {
            override fun onEnd() {
                isEnded = true
            }
        })

        taskManager.run(componentId, Custom1TaskStore("root", 2, 3))
        // T1
        // ├ E1
        // ├ E2
        // ├ E3
        // ├ T1-1
        // │ ├ E1
        // │ ├ E2
        // │ └ E3
        // ├ E1
        // ├ E2
        // ├ E3
        // ├ T1-2
        // │ ├ E1
        // │ ├ E2
        // │ └ E3
        // ├ E1
        // ├ E2
        // └ E3
        // T: 3, E: 15, Time: 0.5 * 15 = 7.5

        taskManager.update(7.4f)
        assertFalse { isEnded }
        taskManager.update(0.1f)
        assertTrue { isEnded }
    }
}
