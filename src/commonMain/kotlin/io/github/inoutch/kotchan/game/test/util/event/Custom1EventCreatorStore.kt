package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventCreatorStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1EventCreatorStore : EventCreatorStore() {
    override val factoryClass: String
        get() = className(Custom1EventCreatorFactory::class)
}
