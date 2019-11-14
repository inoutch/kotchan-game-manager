package io.github.inoutch.kotchan.game.action.store

import kotlinx.serialization.Serializable

@Serializable
class TaskRuntimeStore(
    override val componentId: String,
    override val id: Long,
    val taskStore: TaskStore
) : ActionRuntimeStore
