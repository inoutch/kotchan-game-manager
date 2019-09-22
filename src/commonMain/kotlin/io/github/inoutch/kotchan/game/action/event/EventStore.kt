package io.github.inoutch.kotchan.game.action.event

import io.github.inoutch.kotchan.game.action.ActionStore
import kotlinx.serialization.Serializable

@Serializable
abstract class EventStore : ActionStore {
    abstract val durationTime: Long
}
