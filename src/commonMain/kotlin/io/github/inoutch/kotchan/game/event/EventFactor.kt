package io.github.inoutch.kotchan.game.event

import kotlinx.serialization.Serializable

@Serializable
abstract class EventFactor : Event {
    abstract val durationTime: Long
}
