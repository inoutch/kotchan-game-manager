package io.github.inoutch.kotchan.game.component.group

import io.github.inoutch.kotchan.game.component.Component
import kotlin.reflect.KClass

open class ComponentGroupByLabel<T : Component>(
        filterClass: KClass<T>,
        val label: String,
        components: List<T>) : ComponentGroup<T>(filterClass, components.filter { filter(it, label) }) {
    companion object {
        private fun filter(component: Component, label: String): Boolean {
            return component.parentId == label || component.labels.contains(label)
        }
    }

    // Listener
    override fun create(component: T) {
        if (filter(component, label)) {
            super.create(component)
        }
    }

    override fun willDestroy() {}
}
