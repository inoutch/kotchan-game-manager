package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventReducer
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventReducer : EventReducer<Custom1EventReducerStore, CustomComponent>(
        Custom1EventReducerStore::class,
        CustomComponent::class) {

    override fun start() {
        component.raw.states.add("event-start")
    }

    override fun update(ratio: Float) {
        component.raw.states.add("event-update")
    }

    override fun end() {
        component.raw.states.add("event-end")
    }
}
