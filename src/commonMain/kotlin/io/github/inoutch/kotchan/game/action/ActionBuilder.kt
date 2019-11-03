package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.event.EventStore
import io.github.inoutch.kotchan.game.action.nop.NoOperationStore
import io.github.inoutch.kotchan.game.action.task.TaskStore

class ActionBuilder {
    val actionStoreQueue: List<ActionStore>
        get() = privateActionStoreQueue

    private val privateActionStoreQueue = mutableListOf<ActionStore>()

    private var current: ActionStore? = null

    private var type = -1

    fun enqueue(actionStore: EventStore) {
        check(type != 1)
        type = 0
        privateActionStoreQueue.add(actionStore)
    }

    fun enqueue(actionStore: TaskStore) {
        check(type != 0)
        type = 1
        privateActionStoreQueue.add(actionStore)
    }

    fun enqueue(noOperationStore: NoOperationStore) {
        privateActionStoreQueue.add(noOperationStore)
    }
}
