package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.custom.Custom1EventRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom1EventStore
import io.github.inoutch.kotchan.game.action.custom.Custom1TaskRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom1TaskStore
import io.github.inoutch.kotchan.game.action.runner.TaskRunner
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class SerializationTest {
    private val module = SerialModule.generate({
        Custom1TaskStore::class with Custom1TaskStore.serializer()
    }, {
        Custom1EventStore::class with Custom1EventStore.serializer()
    })

    private val protoBuf = Json(context = module)

    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())
    }

    @Test
    fun checkSerializableTaskManager() {
        val component1Store = CustomStore("action")

        val component1Id = componentManager.createComponent(component1Store)
        assertNotNull(component1Id)

        val component1 = componentManager.findById(component1Id, CustomComponent::class)
        assertNotNull(component1)

        var expectIsEnded = false
        val expectedTaskManager = TaskManager(object : TaskManager.Action {
            override fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>) {
                expectIsEnded = true
            }
        })
        expectedTaskManager.registerFactory(Custom1TaskRunnerFactory())
        expectedTaskManager.registerComponent(component1Id)

        expectedTaskManager.run(component1Id, Custom1TaskStore("root", 2, 3, false))
        val data = expectedTaskManager.serialize()
        val bytes = protoBuf.stringify(TaskManager.InitData.serializer(), data)
        assertTrue { bytes.isNotEmpty() }

        expectedTaskManager.update(7.4f)
        componentManager.destroyAllComponents()

        var actualIsEnded = false
        val actualInitData = protoBuf.parse(TaskManager.InitData.serializer(), bytes)
        val actualTaskManager = TaskManager(object : TaskManager.Action {
            override fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>) {
                actualIsEnded = true
            }
        }, actualInitData)

        val component2Id = componentManager.createComponent(component1Store)
        assertNotNull(component2Id)

        val component2 = componentManager.findById(component2Id, CustomComponent::class)
        assertNotNull(component2)

        actualTaskManager.registerFactory(Custom1TaskRunnerFactory())
//        actualTaskManager.registerComponent(component2Id)

        actualTaskManager.update(7.4f)
        assertEquals(component1.raw.history, component2.raw.history)
        assertEquals(expectIsEnded, actualIsEnded)

        expectedTaskManager.update(0.1f)
        actualTaskManager.update(0.1f)
        assertEquals(component1.raw.history, component2.raw.history)
        assertEquals(expectIsEnded, actualIsEnded)
    }

    @Test
    fun checkSerializableEventManager() {
        val component1Id = componentManager.createComponent(CustomStore("action"))
        assertNotNull(component1Id)

        val component1 = componentManager.findById(component1Id, CustomComponent::class)
        assertNotNull(component1)

        val taskManager = TaskManager(object : TaskManager.Action {
            override fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>) {}
        })
        taskManager.registerFactory(Custom1TaskRunnerFactory())
        taskManager.registerComponent(component1Id)

        var eventManager = EventManager()
        eventManager.registerFactory(Custom1EventRunnerFactory())

        taskManager.addTaskListener(eventManager)

        // T
        // ├ E1
        // ├ E2
        // ├ E3
        // ├ T1
        // │ ├ E1
        // │ ├ E2
        // │ └ E3
        // ├ E1
        // ├ E2
        // ├ E3
        // ├ T2
        // │ ├ E1
        // │ ├ E2
        // │ └ E3
        // ├ E1
        // ├ E2
        // └ E3
        // T: 3, E: 15, Time: 0.5 * 15 = 7.5
        taskManager.run(component1Id, Custom1TaskStore("root", 2, 3, false))
        taskManager.update(7.5f)

        val expectedStore = eventManager.store()
        val bytes = protoBuf.stringify(EventManager.Store.serializer(), expectedStore)

        eventManager.update(taskManager.currentTime)

        val history = component1.raw.history.toList()
        component1.raw.history.clear()

        val actualStore = protoBuf.parse(EventManager.Store.serializer(), bytes)
        eventManager = EventManager()
        eventManager.registerFactory(Custom1EventRunnerFactory())
        eventManager.restore(actualStore)

        eventManager.update(taskManager.currentTime)

        assertEquals(history, component1.raw.history)
    }
}
