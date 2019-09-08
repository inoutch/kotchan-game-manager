package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventReducerStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1EventReducerStore(override val durationTime: Long) : EventReducerStore() {

    override val factoryClass: String
        get() = className(Custom1EventReducerFactory::class)
}
