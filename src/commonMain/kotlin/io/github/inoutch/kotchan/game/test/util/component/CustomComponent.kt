package io.github.inoutch.kotchan.game.test.util.component

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.test.util.component.store.CustomStore

class CustomComponent : Component() {
    var state: String = "none"

    val customValue = store<CustomStore>().customValue

    var processed = "none"

    var ratio = 0.0f

    override fun create() {
        super.create()
        state = "created"
    }

    override fun update(delta: Float) {
        super.update(delta)
        state = "updated"
    }

    override fun willDestroy() {
        super.willDestroy()
        state = "will-destroy"
    }

    override fun destroyed() {
        super.destroyed()
        state = "destroyed"
    }
}
