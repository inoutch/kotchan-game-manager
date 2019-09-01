package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventCreator
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1EventCreator : EventCreator() {
    override val factoryClass: String
        get() = className(Custom1EventCreatorRunnerFactory::class)
}
