package io.github.inoutch.kotchan.game.component

import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.contextProvider
import io.github.inoutch.kotchan.game.component.store.Store
import io.github.inoutch.kotchan.game.error.ERR_V_MSG_0
import io.github.inoutch.kotchan.game.error.ERR_V_MSG_2
import io.github.inoutch.kotchan.game.extension.fastForEach

abstract class Component {
    val store = contextProvider.current.store

    val id = store.id

    val parentId: String
        get() = store.parentId

    val labels: List<String>
        get() = store.labels

    // DO NOT CHANGE BY SUBCLASS
    var lifecycle: ComponentLifecycle = ComponentLifecycle.CREATE

    open var updatable: Boolean = true
        set(value) {
            if (value == field) {
                return
            }
            field = value
            if (value) {
                componentManager.startToUpdate(this)
            } else {
                componentManager.stopToUpdate(this)
            }
        }

    private val listeners = mutableListOf<ComponentLifecycleListener<Component>>()

    inline fun <reified T : Store> store(): T {
        return contextProvider.current.store()
    }

    open fun create() {
        listeners.fastForEach { it.create(this) }
    }

    open fun update(delta: Float) {}

    open fun willDestroy() {}

    open fun destroyed() {
        listeners.fastForEach { it.destroyed(this) }
    }

    fun <T : Component> addLifecycleListener(listener: ComponentLifecycleListener<T>) {
        @Suppress("UNCHECKED_CAST")
        listeners.add(listener as ComponentLifecycleListener<Component>)
    }

    fun removeLifecycleListener(listener: ComponentLifecycleListener<*>) {
        listeners.remove(listener)
    }

    protected inline fun <reified T : Component> parent(): T {
        check(lifecycle != ComponentLifecycle.CREATE) { ERR_V_MSG_0 }
        return componentManager.findById(parentId, T::class)?.raw ?: throw IllegalStateException(ERR_V_MSG_2)
    }
}
