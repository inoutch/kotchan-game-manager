package io.github.inoutch.kotchan.game.component

import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.component.*
import io.github.inoutch.kotchan.game.test.util.component.store.CustomChildStore
import io.github.inoutch.kotchan.game.test.util.component.store.CustomNoUpdateStore
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import io.github.inoutch.kotchan.game.util.Mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ComponentAccessorTest {
    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())
        componentManager.registerComponentFactory(CustomNoUpdateComponentFactory())
        componentManager.registerComponentFactory(CustomChildComponentFactory())
    }

    @Test
    fun checkAccessor() {
        val componentId = componentManager.createComponent(CustomNoUpdateStore("test"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomNoUpdateComponent::class)
        assertNotNull(component)

        var access = ""

        assertEquals(ComponentLifecycle.CREATE, component.lifecycle)
        component.access { l, _ ->
            access = when (l) {
                ComponentLifecycle.CREATE -> "create"
                ComponentLifecycle.UPDATE -> "update"
                else -> "error"
            }
        }
        assertEquals("create", access)

        assertEquals(ComponentLifecycle.CREATE, component.lifecycle)
        component.access { l, _ ->
            access = when (l) {
                ComponentLifecycle.CREATE -> "create"
                ComponentLifecycle.UPDATE -> "update"
                else -> "error"
            }
        }
        assertEquals("create", access)

        componentManager.update(Mock.DELTA_TIME)

        assertEquals(ComponentLifecycle.UPDATE, component.lifecycle)
        component.access { l, _ ->
            access = when (l) {
                ComponentLifecycle.CREATE -> "create"
                ComponentLifecycle.UPDATE -> "update"
                else -> "error"
            }
        }
        assertEquals("update", access)

        componentManager.reserveComponentDestruction(component)

        assertEquals(ComponentLifecycle.WILL_DESTROY, component.lifecycle)
        component.access { l, _ ->
            access = when (l) {
                ComponentLifecycle.CREATE -> "create"
                ComponentLifecycle.UPDATE -> "update"
                ComponentLifecycle.WILL_DESTROY -> "update"
                else -> "error"
            }
        }
        assertEquals("update", access)

        componentManager.update(Mock.DELTA_TIME)

        assertEquals(ComponentLifecycle.DESTROYED, component.lifecycle)
        component.access { l, _ ->
            access = when (l) {
                ComponentLifecycle.CREATE -> "create"
                ComponentLifecycle.UPDATE -> "update"
                ComponentLifecycle.DESTROYED -> "destroy"
                else -> "error"
            }
        }
        assertEquals("destroy", access)
    }

    @Test
    fun checkToAccessChildComponent() {
        val parentId = componentManager.createComponent(CustomStore("test"))
        assertNotNull(parentId)

        val parent = componentManager.findById(parentId, CustomComponent::class)
        assertNotNull(parent)

        val childId = componentManager.createComponent(CustomChildStore("test"), parentId)
        assertNotNull(childId)

        val child = componentManager.findById(childId, CustomChildComponent::class)
        assertNotNull(child)

        assertEquals(ComponentLifecycle.CREATE, child.lifecycle)
        componentManager.update(Mock.DELTA_TIME)

        child.access { l, c ->
            when (l) {
                ComponentLifecycle.CREATE -> {
                    assertNotNull(parentId, c.component.id)
                }
                else -> {
                }
            }
        }
    }
}
