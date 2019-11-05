package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.custom.Custom1EventRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom1TaskRunnerFactory
import io.github.inoutch.kotchan.game.action.custom.Custom1TaskStore
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

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

        eventManager.registerFactory(Custom1EventRunnerFactory())
        taskManager.registerFactory(Custom1TaskRunnerFactory())

        taskManager.registerComponent(componentId)

        taskManager.run(componentId, Custom1TaskStore(3))
    }
}
