package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventCreator
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom2EventCreator(val name: String) : EventCreator() {
    override val factoryClass: String = className(Custom2EventCreatorRunnerFactory::class)
}
