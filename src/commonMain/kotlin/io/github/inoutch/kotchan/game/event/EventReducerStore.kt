package io.github.inoutch.kotchan.game.event

import kotlinx.serialization.Serializable

@Serializable
abstract class EventReducerStore : EventStore {
    abstract val durationTime: Long
}
