package io.github.inoutch.kotchan.game.action

interface ActionRuntimeStore {
    val id: Long
    val parentId: Long
    val componentId: String
}
