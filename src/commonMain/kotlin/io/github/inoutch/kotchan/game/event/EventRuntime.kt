package io.github.inoutch.kotchan.game.event

import kotlinx.serialization.Serializable

@Serializable
data class EventRuntime(
        val id: Long,
        val componentId: String,
        val eventStore: EventStore,
        val startTime: Long) {
    val endTime: Long
        get() = startTime + if (eventStore is EventReducerStore) eventStore.durationTime else 0
}
