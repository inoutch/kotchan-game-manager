package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.store.EventStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1EventStore(
        val parentName: String,
        val eventName: String,
        val allowInterrupt: Boolean,
        override val durationTime: Long) : EventStore() {
    override val factoryClass: String
        get() = className(Custom1EventRunnerFactory::class)
}
