package io.github.inoutch.kotchan.game.component

import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.test.util.component.*
import io.github.inoutch.kotchan.game.test.util.component.store.*
import io.github.inoutch.kotchan.game.util.Mock
import kotlin.test.*

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
    fun checkToUpdateComponent() {
        val componentId = componentManager.createComponent(CustomNoUpdateStore("test"))
        assertNotNull(componentId)

        val component = componentManager.findById(componentId, CustomNoUpdateComponent::class)
        assertNotNull(component)

        assertEquals(ComponentLifecycle.CREATE, component.lifecycle)
        component.access { l, c ->
            when (l) {
                ComponentLifecycle.CREATE -> {
                    assertEquals("none", c.status)
                }
                else -> {
                }
            }
        }

        componentManager.update(Mock.DELTA_TIME)

        assertEquals(ComponentLifecycle.UPDATE, component.lifecycle)
        component.access { l, c ->
            when (l) {
                ComponentLifecycle.UPDATE -> {
                    assertEquals("created", c.status)
                }
                else -> {
                }
            }
        }
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
}
