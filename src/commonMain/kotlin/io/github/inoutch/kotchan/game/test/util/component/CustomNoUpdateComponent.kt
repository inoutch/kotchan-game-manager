package io.github.inoutch.kotchan.game.test.util.component

import io.github.inoutch.kotchan.game.component.Component

class CustomNoUpdateComponent : Component() {
    override var updatable: Boolean = false

    var status: String = "none"
        private set

    override fun create() {
        status = "created"
    }

    override fun update(delta: Float) {
        status = "updated"
    }

    override fun willDestroy() {
        status = "will-destroy"
    }

    override fun destroyed() {
        status = "destroyed"
    }
}
