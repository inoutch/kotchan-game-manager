package io.github.inoutch.kotchan.game.action.context

import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.action.store.TaskStore
import io.github.inoutch.kotchan.game.util.tree.SerializableTree

data class ActionComponentContext(
        val tree: SerializableTree<TaskStore>,
        val eventRuntimeStores: MutableList<EventRuntimeStore>,
        var currentNodeId: Long,
        var interrupting: Boolean = false)
