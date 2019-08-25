package io.github.inoutch.kotchan.game.component

class ComponentAccessor<T : Component> private constructor(val raw: T) {
    companion object {
        fun <T : Component> create(component: T): ComponentAccessor<T> {
            return ComponentAccessor(component)
        }
    }

    val lifecycle: ComponentLifecycle
        get() = raw.lifecycle

    inline fun access(scope: (lifecycle: ComponentLifecycle, component: T) -> Unit) {
        scope(raw.lifecycle, raw)
    }
}
