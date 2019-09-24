package io.github.inoutch.kotchan.game.test.util.action.event

import io.github.inoutch.kotchan.game.action.event.EventStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1EventStore(
        val customValue: String,
        override val durationTime: Long) : EventStore() {
    override val factoryClass: String
        get() = className(Custom1EventRunnerFactory::class)
}
