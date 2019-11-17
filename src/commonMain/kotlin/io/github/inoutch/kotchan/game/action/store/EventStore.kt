package io.github.inoutch.kotchan.game.action.store

import kotlinx.serialization.Serializable

@Serializable
abstract class EventStore : ActionStore {
    abstract val durationTime: Long

    abstract fun isInterruptible(): Boolean
}
