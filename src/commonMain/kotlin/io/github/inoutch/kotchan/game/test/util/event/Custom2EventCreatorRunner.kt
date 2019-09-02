package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventBuilder
import io.github.inoutch.kotchan.game.event.EventCreatorRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom2EventCreatorRunner : EventCreatorRunner<Custom2EventCreator, CustomComponent>(
        Custom2EventCreator::class,
        CustomComponent::class) {
    override fun next(builder: EventBuilder) {
        if (component.raw.increment++ >= 3) {
            return
        }

        builder.enqueue(Custom2EventCreator("custom-2-event-creator-runner"))
        builder.enqueue(Custom1EventFactor(100))
    }
}
