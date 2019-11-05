package io.github.inoutch.kotchan.game.action.builder

open class ActionBuilder<T> {
    val actionStoreQueue: List<T>
        get() = privateActionStoreQueue

    private val privateActionStoreQueue = mutableListOf<T>()

    fun enqueue(actionStore: T) {
        privateActionStoreQueue.add(actionStore)
    }
}
