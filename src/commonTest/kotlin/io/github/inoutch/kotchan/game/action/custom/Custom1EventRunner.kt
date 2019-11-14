package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.runner.EventRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventRunner : EventRunner<Custom1EventStore, CustomComponent>(Custom1EventStore::class, CustomComponent::class) {
    override val updatable = true

    override fun start() {
        component.raw.history.add("${store.parentName}:${store.eventName}:s")
    }

    override fun end() {
        component.raw.history.add("${store.parentName}:${store.eventName}:e")
    }

    override fun update(ratio: Float) {
        component.raw.history.add("${store.parentName}:${store.eventName}:u")
    }

    override fun allowInterrupt(): Boolean {
        component.raw.history.add("${store.parentName}:${store.eventName}:a")
        return store.allowInterrupt
    }

    override fun interrupt() {
        component.raw.history.add("${store.parentName}:${store.eventName}:i")
    }
}
