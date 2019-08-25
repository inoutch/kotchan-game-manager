package io.github.inoutch.kotchan.game.component.group

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentAccessor
import io.github.inoutch.kotchan.game.component.ComponentLifecycleListener
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.extension.checkClass
import kotlin.reflect.KClass

abstract class ComponentGroup<T : Component>(
        private val kClass: KClass<T>,
        components: List<T>) : Component(), ComponentLifecycleListener<T> {

    init {
        // CAUTION: CALL BEFORE CREATE FUNC
        @Suppress("LeakingThis")
        componentManager.registerComponentListener(this, kClass)
    }

    protected val components = components.toMutableList()

    val size: Int
        get() = components.size

    val readOnlyComponents: List<T>
        get() = components

    inline fun <reified T : Component> get(index: Int): ComponentAccessor<T>? {
        val component = readOnlyComponents.getOrNull(index)
        if (component is T) {
            return ComponentAccessor.create(component)
        }
        return null
    }

    inline fun <reified T : Component> find(predicate: (component: T) -> Boolean): ComponentAccessor<T>? {
        val component = readOnlyComponents
                .filterIsInstance<T>()
                .find(predicate) ?: return null
        return ComponentAccessor.create(component)
    }

    override fun create() {}

    override fun update(delta: Float) {}

    override fun create(component: T) {
        component.checkClass(kClass)?.let { this.components.add(it) }
    }

    override fun destroyed(component: T) {
        this.components.remove(component)
    }

    override fun destroyed() {
        componentManager.unregisterComponentListener(this)
    }
}
