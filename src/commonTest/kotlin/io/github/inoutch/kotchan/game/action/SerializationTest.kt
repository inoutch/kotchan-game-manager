package io.github.inoutch.kotchan.game.action

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
    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())
    }

    @Test
    fun checkSerializableServer() {
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

        expectedTaskManager.run(component1Id, Custom1TaskStore("root", 2, 3))
        val data = expectedTaskManager.serialize()

        val module = SerialModule.generate({
            Custom1TaskStore::class with Custom1TaskStore.serializer()
        }, {
            Custom1EventStore::class with Custom1EventStore.serializer()
        })
        val protoBuf = Json(context = module)
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
}
