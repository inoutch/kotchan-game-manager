package io.github.inoutch.kotchan.game.event

import kotlinx.serialization.Serializable

@Serializable
class EventReducerStoreCancel(val interruptable: Boolean) : EventReducerStore() {
    override val durationTime = 0L
    override val factoryClass = ""
}
