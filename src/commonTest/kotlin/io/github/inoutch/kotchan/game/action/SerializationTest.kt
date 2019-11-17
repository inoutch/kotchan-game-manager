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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.protobuf.ProtoBuf

class SerializationTest {
    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())
    }

    @Test
    fun checkSerializableServer() {
        val targetComponentId = componentManager.createComponent(CustomStore("action"))
        assertNotNull(targetComponentId)

        val component = componentManager.findById(targetComponentId, CustomComponent::class)
        assertNotNull(component)

        var isEnded = false
        val taskManager = TaskManager(object : TaskManager.Action {
            override fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>) {
                isEnded = true
                assertEquals(componentId, targetComponentId)
            }
        })

        taskManager.registerFactory(Custom1TaskRunnerFactory())
        taskManager.registerComponent(targetComponentId)

        taskManager.run(targetComponentId, Custom1TaskStore("root", 2, 3))
        val data = taskManager.serialize()

        val module = SerialModule.generate({
            Custom1TaskStore::class with Custom1TaskStore.serializer()
        }, {
            Custom1EventStore::class with Custom1EventStore.serializer()
        })
        val protoBuf = ProtoBuf(context = module)
        val bytes = protoBuf.dump(TaskManager.InitData.serializer(), data)
        assertTrue { bytes.isNotEmpty() }
        assertFalse { isEnded }
    }
}
