package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventCreatorStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom2EventCreatorStore(val name: String) : EventCreatorStore() {
    override val factoryClass: String = className(Custom2EventCreatorFactory::class)
}
