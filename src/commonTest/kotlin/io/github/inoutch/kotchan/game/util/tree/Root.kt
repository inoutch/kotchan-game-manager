package io.github.inoutch.kotchan.game.util.tree

import kotlinx.serialization.Serializable

@Serializable
data class Root(val array: List<SerializableNode<String>>)

