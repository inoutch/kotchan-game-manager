package io.github.inoutch.kotchan.game.component

import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.component.CustomChildComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomChildComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.CustomNoUpdateComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore
import io.github.inoutch.kotchan.game.util.Mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ComponentManagerTest {
    @BeforeTest
    fun init() {
        componentManager.destroyAllComponents()
        componentManager.unregisterAllComponentFactories()
        componentManager.registerComponentFactory(CustomComponentFactory())
        componentManager.registerComponentFactory(CustomNoUpdateComponentFactory())
        componentManager.registerComponentFactory(CustomChildComponentFactory())
    }

    @Test
    fun checkStandardComponentLifecycle() {
        val componentId = componentManager.createComponent(CustomStore("test"))
        assertNotNull(componentId)

        val accessor = componentManager.findById(componentId, CustomComponent::class)
        assertNotNull(accessor)

        assertEquals(ComponentLifecycle.CREATE, accessor.lifecycle)
        accessor.access { l, c ->
            when (l) {
                ComponentLifecycle.CREATE -> {
                    assertEquals(c.lifecycle, ComponentLifecycle.CREATE)
                    assertEquals("none", c.state)
                }
                else -> {
                }
            }
        }

        componentManager.update(Mock.DELTA_TIME)

        assertEquals(ComponentLifecycle.UPDATE, accessor.lifecycle)
        accessor.access { l, c ->
            when (l) {
                ComponentLifecycle.UPDATE -> {
                    assertEquals(ComponentLifecycle.UPDATE, c.lifecycle)
                    assertEquals("updated", c.state)
                }
                else -> {
                }
            }
        }

        componentManager.reserveComponentDestruction(accessor)

        assertEquals(ComponentLifecycle.WILL_DESTROY, accessor.lifecycle)
        accessor.access { l, c ->
            when (l) {
                ComponentLifecycle.UPDATE -> {
                    assertEquals("will-destroy", c.state)
                    assertEquals(ComponentLifecycle.WILL_DESTROY, c.lifecycle)
                }
                else -> {
                }
            }
        }

        componentManager.update(Mock.DELTA_TIME)

        assertEquals(0, componentManager.componentSize)
    }

    @Test
    fun checkRelationsOfParentComponent() {
        val parentId = componentManager.createComponent(CustomStore("test"))
        assertNotNull(parentId)

        val parent = componentManager.findById(parentId, CustomComponent::class)
        assertNotNull(parent)

        componentManager.createComponent(CustomStore("test"), parentId)
        val children = componentManager.createComponentGroupByLabel(CustomComponent::class, parentId)

        assertNotNull(children)
        assertEquals(ComponentLifecycle.CREATE, children.lifecycle)
        children.access { l, c ->
            when (l) {
                ComponentLifecycle.CREATE -> {
                    assertEquals(0, c.readOnlyComponents.size)
                }
                else -> {
                }
            }
        }

        componentManager.update(Mock.DELTA_TIME)
        children.access { l, c ->
            when (l) {
                ComponentLifecycle.CREATE -> {
                    assertEquals(1, c.readOnlyComponents.size)
                }
                else -> {
                }
            }
        }

        componentManager.createComponent(CustomStore("test"), parentId)

        componentManager.update(Mock.DELTA_TIME)

        assertEquals(ComponentLifecycle.UPDATE, children.lifecycle)
        children.access { l, c ->
            when (l) {
                ComponentLifecycle.UPDATE -> {
                    assertEquals(2, c.readOnlyComponents.size)
                }
                else -> {
                }
            }
        }

        componentManager.reserveComponentDestruction(parent)

        componentManager.update(Mock.DELTA_TIME)

        assertEquals(ComponentLifecycle.UPDATE, children.lifecycle)
        children.access { l, c ->
            when (l) {
                ComponentLifecycle.UPDATE -> {
                    assertEquals(0, c.readOnlyComponents.size)
                }
                else -> {
                }
            }
        }

        componentManager.reserveComponentDestruction(children)
        componentManager.update(Mock.DELTA_TIME)

        assertEquals(0, componentManager.componentSize)
    }

    @Test
    fun checkLazyLoadComponent() {
        componentManager.createComponentsIntermittently(
                listOf(CustomStore("lazy1"),
                        CustomStore("lazy2"),
                        CustomStore("lazy3"))) {
            componentManager.activateAllComponents()
        }

        componentManager.update(Mock.DELTA_TIME)

        assertEquals(3, componentManager.componentSize)
    }

    @Test
    fun checkFindById() {
        val notFound = componentManager.findById("component-12345", CustomComponent::class)
        assertNull(notFound)

        val customComponentId = componentManager.createComponent(CustomStore("ABC"))
        assertNotNull(customComponentId)

        val customComponent1 = componentManager.findById(customComponentId, CustomComponent::class)
        assertNotNull(customComponent1)
        assertEquals("ABC", customComponent1.raw.customValue)

        val customComponent2 = componentManager.findById(customComponentId, CustomChildComponent::class)
        assertNull(customComponent2)
    }

    @Test
    fun checkSubscription() {
        val customComponentId = componentManager.createComponent(CustomStore("ABC"))
        assertNotNull(customComponentId)

        var status = "none"
        val listener = object : ComponentLifecycleListener<CustomComponent> {
            override fun create(component: CustomComponent) {
                status = "create"
            }

            override fun destroyed(component: CustomComponent) {
                status = "destroyed"
            }
        }

        componentManager.subscribe(customComponentId, CustomComponent::class, listener)

        assertEquals("none", status)

        componentManager.update(Mock.DELTA_TIME)

        assertEquals("create", status)

        componentManager.reserveComponentDestruction(customComponentId)

        assertEquals("create", status)

        componentManager.update(Mock.DELTA_TIME)

        assertEquals("destroyed", status)
    }
}
