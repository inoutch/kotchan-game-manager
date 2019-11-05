package io.github.inoutch.kotchan.game.action.store

import kotlinx.serialization.Serializable

@Serializable
class EventRuntimeStore(
        override val componentId: String,
        override val id: Long,
        val startTime: Long,
        val eventStore: EventStore) : ActionRuntimeStore {
    fun endTime() = startTime + eventStore.durationTime
}
