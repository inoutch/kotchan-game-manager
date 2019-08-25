package io.github.inoutch.kotchan.game.component

interface ComponentLifecycleListener<T: Component> {
    fun create(component: T)
    fun destroyed(component: T)
}
