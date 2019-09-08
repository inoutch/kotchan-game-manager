package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventBuilder
import io.github.inoutch.kotchan.game.event.EventCreator
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom2EventCreator : EventCreator<Custom2EventCreatorStore, CustomComponent>(
        Custom2EventCreatorStore::class,
        CustomComponent::class) {
    override fun next(builder: EventBuilder) {
        if (component.raw.increment++ >= 3) {
            return
        }

        builder.enqueue(Custom2EventCreatorStore("custom-2-event-creator-runner"))
        builder.enqueue(Custom1EventReducerStore(100))
    }
}
