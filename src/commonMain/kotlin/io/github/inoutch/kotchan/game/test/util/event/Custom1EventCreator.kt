package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventBuilder
import io.github.inoutch.kotchan.game.event.EventCreator
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventCreator : EventCreator<Custom1EventCreatorStore, CustomComponent>(
        Custom1EventCreatorStore::class,
        CustomComponent::class) {
    override fun next(builder: EventBuilder) {
        builder.enqueue(Custom1EventReducerStore(500))
    }
}
