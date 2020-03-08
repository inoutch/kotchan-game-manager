package io.github.inoutch.kotchan.game.util.tree

import io.github.inoutch.kotchan.game.extension.fastForEach
import io.github.inoutch.kotchan.game.util.IdManager

class SerializableTree<T> private constructor(initialNodes: List<SerializableNode<T>>) {
    companion object {
        fun <T> create(initialNodes: List<SerializableNode<T>> = emptyList()): SerializableTree<T> {
            return SerializableTree(initialNodes)
        }
    }

    private var idManager = IdManager()

    private val nodes = mutableMapOf<Long, SerializableNode<T>>()

    private val nodesByParent = mutableMapOf<Long, MutableList<SerializableNode<T>>>()

    val size: Int
        get() = nodes.size

    init {
        var maxId = -1L
        initialNodes.fastForEach {
            nodes[it.id] = it
            if (maxId < it.id) {
                maxId = it.id
            }
            nodesByParent.getOrPut(it.parentId) { mutableListOf() }.add(it)
        }
        idManager.reset(maxId + 1)
    }

    operator fun get(id: Long): SerializableNode<T>? {
        return nodes[id]
    }

    fun getValue(id: Long): SerializableNode<T> {
        return nodes.getValue(id)
    }

    fun get(id: Long, childIndex: Int): SerializableNode<T>? {
        return nodesByParent.getValue(id).getOrNull(childIndex)
    }

    fun add(value: T, parentId: Long = -1, id: Long? = null): SerializableNode<T> {
        if (parentId == -1L) {
            check(nodes.isEmpty()) { "A root node has already been added." }
        } else {
            check(nodes.contains(parentId)) { "Parent node is not added. [ID = $parentId]" }
        }

        var nextId = id
        if (nextId == null) {
            nextId = idManager.getAndIncrementNextId()
        } else {
            check(nextId >= idManager.nextId) { "Manually added id must set a value greater than the managed id. [Manually id = $nextId, managed id ${idManager.nextId}]" }
            idManager.reset(nextId)
        }
        val node = SerializableNode(nextId, value, parentId)
        nodes[node.id] = node
        nodesByParent.getOrPut(parentId) { mutableListOf() }.add(node)
        return node
    }

    fun remove(id: Long) {
        nodesByParent[id]?.fastForEach { remove(it.id) }
        nodes.remove(id)
    }

    fun removeChildren(parentId: Long) {
        nodesByParent.getValue(parentId).clear()
    }

    fun removeFromParent(parentId: Long, index: Int) {
        nodesByParent.getValue(parentId).removeAt(index)
    }

    fun toArray(): List<SerializableNode<T>> {
        return nodes.values.toList()
    }
}
