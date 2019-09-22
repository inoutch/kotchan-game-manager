package io.github.inoutch.kotchan.game.action.task

import io.github.inoutch.kotchan.game.action.ActionRuntimeStore
import kotlinx.serialization.Serializable

@Serializable
class TaskRuntimeStore(
        override val componentId: String,
        override val id: Long,
        override val parentId: Long,
        val taskStore: TaskStore) : ActionRuntimeStore
