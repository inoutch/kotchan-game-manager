package io.github.inoutch.kotchan.game.test.util.action.event

import io.github.inoutch.kotchan.game.action.event.EventRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventRunner : EventRunner<Custom1EventStore, CustomComponent>(Custom1EventStore::class, CustomComponent::class) {
    override fun start() {
    }

    override fun update(ratio: Float) {
    }

    override fun end() {
    }

    override fun interrupt() {
    }
}
