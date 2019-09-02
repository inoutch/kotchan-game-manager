package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventFactorRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventFactorRunner : EventFactorRunner<Custom1EventFactor, CustomComponent>(
        Custom1EventFactor::class,
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
