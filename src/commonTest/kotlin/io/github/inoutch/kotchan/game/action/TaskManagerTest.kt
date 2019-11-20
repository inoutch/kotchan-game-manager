package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.custom.Custom1EventRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom1TaskRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom1TaskStore
import io.github.inoutch.kotchan.game.action.custom.Custom2TaskRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom2TaskStore
import io.github.inoutch.kotchan.game.action.runner.TaskRunner
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaskManagerTest {
    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())
    }

    @Test
    fun checkIntegration() {
        val componentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        var isEnded = false
        val eventManager = EventManager()
        val taskManager = TaskManager()
        taskManager.addComponentListener(componentId, object : TaskManager.ComponentListener {
            override fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>) {
                isEnded = true
            }
        })
        taskManager.addTaskListener(eventManager)

        eventManager.registerFactory(Custom1EventRunnerFactory())
        taskManager.registerFactory(Custom1TaskRunnerFactory())

        val history = mutableListOf<String>()
        taskManager.registerComponent(componentId)

        taskManager.run(componentId, Custom1TaskStore("root", 2, 2, false))

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

        var isEnded = false
        val eventManager = EventManager()
        val taskManager = TaskManager()
        taskManager.addComponentListener(componentId, object : TaskManager.ComponentListener {
            override fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>) {
                isEnded = true
            }
        })
        taskManager.addTaskListener(eventManager)

        eventManager.registerFactory(Custom1EventRunnerFactory())
        taskManager.registerFactory(Custom1TaskRunnerFactory())

        taskManager.registerComponent(componentId)

        taskManager.run(componentId, Custom1TaskStore("root", 2, 3, false))
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

    @Test
    fun checkInterruptFalse() {
        val componentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        var isEnded = false
        val taskManager = TaskManager()
        taskManager.addComponentListener(componentId, object : TaskManager.ComponentListener {
            override fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>) {
                isEnded = true
            }
        })
        taskManager.registerFactory(Custom2TaskRunnerFactory())

        val eventManager = EventManager()
        eventManager.registerFactory(Custom1EventRunnerFactory())
        taskManager.addTaskListener(eventManager)

        taskManager.registerComponent(componentId)
        taskManager.run(componentId, Custom2TaskStore("root", 2, 2, false))
        // T
        // ├ E1
        // ├ E2
        // ├ T1
        // │ ├ E1
        // │ ├ E2
        // │ ├ T1-1
        // │ │ ├ E1
        // │ │ └ E2
        // │ ├ E1 <--- interrupt
        // │ └ E2
        // ├ E1
        // ├ E2
        // ├ T2
        // │ ├ E1
        // │ ├ E2
        // │ └ T2-1
        // │ 　 ├ E1
        // │ 　 └ E2
        // ├ E1
        // └ E2
        taskManager.update(0.5f * 6 + 0.3f)
        eventManager.update(taskManager.currentTime)

        assertFalse { isEnded }
        taskManager.interrupt(componentId)
        taskManager.update(0.2f)
        eventManager.update(taskManager.currentTime)

        assertTrue { isEnded }
        val history = listOf(
                "root:e1:s",
                "root:e1:e",
                "root:e2:s",
                "root:e2:e",
                "root-t1:e1:s",
                "root-t1:e1:e",
                "root-t1:e2:s",
                "root-t1:e2:e",
                "root-t1-t1:e1:s",
                "root-t1-t1:e1:e",
                "root-t1-t1:e2:s",
                "root-t1-t1:e2:e",
                "root-t1:e1:s",
                "root-t1:e1:u")
        assertEquals(history, component.raw.history)
    }

    @Test
    fun checkInterruptTrue() {
        val componentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(component)

        var isEnded = false
        val taskManager = TaskManager()
        taskManager.addComponentListener(componentId, object : TaskManager.ComponentListener {
            override fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>) {
                isEnded = true
            }
        })
        taskManager.registerFactory(Custom2TaskRunnerFactory())

        val eventManager = EventManager()
        eventManager.registerFactory(Custom1EventRunnerFactory())
        taskManager.addTaskListener(eventManager)

        taskManager.registerComponent(componentId)
        taskManager.run(componentId, Custom2TaskStore("root", 2, 2, true))
        // T
        // ├ E1
        // ├ E2
        // ├ T1
        // │ ├ E1
        // │ ├ E2
        // │ ├ T1-1
        // │ │ ├ E1
        // │ │ └ E2
        // │ ├ E1 <--- interrupt
        // │ └ E2
        // ├ E1
        // ├ E2
        // ├ T2
        // │ ├ E1
        // │ ├ E2
        // │ └ T2-1
        // │ 　 ├ E1
        // │ 　 └ E2
        // ├ E1
        // └ E2
        taskManager.update(0.5f * 6 + 0.3f)
        eventManager.update(taskManager.currentTime)

        assertFalse { isEnded }
        taskManager.interrupt(componentId)
        taskManager.update(0.2f)
        eventManager.update(taskManager.currentTime)

        assertTrue { isEnded }
        val history = listOf(
                "root:e1:s",
                "root:e1:e",
                "root:e2:s",
                "root:e2:e",
                "root-t1:e1:s",
                "root-t1:e1:e",
                "root-t1:e2:s",
                "root-t1:e2:e",
                "root-t1-t1:e1:s",
                "root-t1-t1:e1:e",
                "root-t1-t1:e2:s",
                "root-t1-t1:e2:e",
                "root-t1:e1:s",
                "root-t1:e1:i",
                "root-t1:e1:u")
        assertEquals(history, component.raw.history)
    }
}
