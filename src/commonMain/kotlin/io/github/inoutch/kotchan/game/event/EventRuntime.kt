package io.github.inoutch.kotchan.game.event

import kotlinx.serialization.Serializable

@Serializable
data class EventRuntime(
        val id: Long,
        val componentId: String,
        val event: Event,
        val startTime: Long) {
    val endTime: Long
        get() = startTime + if (event is EventFactor) event.durationTime else 0
}
