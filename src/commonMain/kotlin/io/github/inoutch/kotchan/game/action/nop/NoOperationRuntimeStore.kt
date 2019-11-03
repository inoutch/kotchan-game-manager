package io.github.inoutch.kotchan.game.action.nop

import io.github.inoutch.kotchan.game.action.ActionRuntimeStore
import kotlinx.serialization.Serializable

@Serializable
class NoOperationRuntimeStore(
        override val id: Long,
        override val componentId: String,
        override val parentId: Long,
        val store: NoOperationStore) : ActionRuntimeStore
