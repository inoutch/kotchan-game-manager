package io.github.inoutch.kotchan.game.test.util.component

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore

class CustomComponent : Component() {
    var state: String = "none"
        private set

    val customValue = store<CustomStore>().customValue

    var processed = "none"

    var ratio = 0.0f

    override fun create() {
        state = "created"
    }

    override fun update(delta: Float) {
        state = "updated"
    }

    override fun willDestroy() {
        state = "will-destroy"
    }

    override fun destroyed() {
        state = "destroyed"
    }
}
