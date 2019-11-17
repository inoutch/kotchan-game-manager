package io.github.inoutch.kotchan.game.action.context

import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.action.store.TaskStore
import io.github.inoutch.kotchan.game.util.tree.SerializableTree

data class ActionComponentContext(
    val tree: SerializableTree<TaskStore>,
    val eventRuntimeStores: MutableList<EventRuntimeStore>,
    var currentNodeId: Long,
    var interrupting: Boolean = false
) {
    companion object {
        fun create(initContext: ActionComponentInitContext): ActionComponentContext {
            return ActionComponentContext(SerializableTree.create(initContext.tree), initContext.eventRuntimeStores.toMutableList(), initContext.currentNodeId, initContext.interrupting)
        }
    }
}
