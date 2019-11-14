package io.github.inoutch.kotchan.game.util.tree

import kotlinx.serialization.Serializable

@Serializable
data class SerializableNode<T>(
    val id: Long,
    val value: T,
    var parentId: Long = -1
)
