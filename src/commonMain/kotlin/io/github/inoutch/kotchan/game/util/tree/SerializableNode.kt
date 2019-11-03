package io.github.inoutch.kotchan.game.util.tree

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.modules.serializersModule

@Serializable
data class SerializableNode<T>(
        val id: Long,
        val value: T,
        var parentId: Long = -1)
