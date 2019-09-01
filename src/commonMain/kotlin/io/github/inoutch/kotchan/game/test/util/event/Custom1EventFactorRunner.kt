package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventFactorRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventFactorRunner : EventFactorRunner<Custom1EventFactor, CustomComponent>(
        Custom1EventFactor::class,
        CustomComponent::class) {

    override fun start() {
        component.raw.state = "event-start"
    }

    override fun update(ratio: Float) {
        component.raw.state = "event-update"
    }

    override fun end() {
        component.raw.state = "event-start"
    }
}
