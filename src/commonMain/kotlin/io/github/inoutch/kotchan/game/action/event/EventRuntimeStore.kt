package io.github.inoutch.kotchan.game.action.event

import io.github.inoutch.kotchan.game.action.ActionRuntimeStore
import kotlinx.serialization.Serializable

@Serializable
class EventRuntimeStore(
        override val componentId: String,
        override val id: Long,
        override val parentId: Long,
        val startTime: Long,
        val eventStore: EventStore) : ActionRuntimeStore
