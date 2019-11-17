package io.github.inoutch.kotchan.game.action.context

import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.action.store.TaskStore
import io.github.inoutch.kotchan.game.util.tree.SerializableNode
import kotlinx.serialization.Serializable

@Serializable
data class ActionComponentInitContext(
    val tree: List<SerializableNode<TaskStore>>,
    val eventRuntimeStores: List<EventRuntimeStore>,
    val currentNodeId: Long,
    var interrupting: Boolean = false
) {
    companion object {
        fun create(context: ActionComponentContext): ActionComponentInitContext {
            return ActionComponentInitContext(context.tree.toArray(), context.eventRuntimeStores, context.currentNodeId, context.interrupting)
        }
    }
}
