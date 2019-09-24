package io.github.inoutch.kotchan.game.test.util.action.event

import io.github.inoutch.kotchan.game.action.event.EventRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventRunner : EventRunner<Custom1EventStore, CustomComponent>(Custom1EventStore::class, CustomComponent::class) {
    override fun start() {
        component.raw.states.add("start-${store.customValue}")
    }

    override fun update(ratio: Float) {
        component.raw.states.add("update-${store.customValue}")
    }

    override fun end() {
        component.raw.states.add("end-${store.customValue}")
    }

    override fun interrupt() {
        component.raw.states.add("interrupt-${store.customValue}")
    }

    override fun allowInterrupt(): Boolean {
        component.raw.states.add("allow-interrupt-${store.customValue}")
        return false
    }
}
