package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventBuilder
import io.github.inoutch.kotchan.game.event.EventCreatorRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventCreatorRunner : EventCreatorRunner<Custom1EventCreator, CustomComponent>(
        Custom1EventCreator::class,
        CustomComponent::class) {
    override fun next(builder: EventBuilder) {
        builder.enqueue(Custom1EventFactor())
    }
}
