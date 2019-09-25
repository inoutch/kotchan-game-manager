package io.github.inoutch.kotchan.game.test.util.action.event

import io.github.inoutch.kotchan.game.action.event.EventRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventRunner : EventRunner<Custom1EventStore, CustomComponent>(Custom1EventStore::class, CustomComponent::class) {
    override fun start() {
        component.raw.states.add("${store.customValue}-e1-start")
    }

    override fun update(ratio: Float) {
        component.raw.states.add("${store.customValue}-e1-update")
    }

    override fun end() {
        component.raw.states.add("${store.customValue}-e1-end")
    }

    override fun interrupt() {
        component.raw.states.add("${store.customValue}-e1-interrupt")
    }

    override fun allowInterrupt(): Boolean {
        component.raw.states.add("${store.customValue}-e1-allow-interrupt")
        return false
    }
}
