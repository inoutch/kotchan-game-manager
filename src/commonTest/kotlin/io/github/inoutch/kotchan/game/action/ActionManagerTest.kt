package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.ActionManager.Companion.actionManager
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.action.event.Custom1EventRunnerFactory
import io.github.inoutch.kotchan.game.test.util.action.event.Custom1EventStore
import io.github.inoutch.kotchan.game.test.util.action.task.Custom1TaskRunnerFactory
import io.github.inoutch.kotchan.game.test.util.action.task.Custom1TaskStore
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

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

        actionManager.registerTaskRunnerFactory(Custom1TaskRunnerFactory())
        actionManager.registerEventRunnerFactory(Custom1EventRunnerFactory())

        val ret = actionManager.run(componentId, Custom1TaskStore("custom1"))
        assertFalse { ret }
    }
}
