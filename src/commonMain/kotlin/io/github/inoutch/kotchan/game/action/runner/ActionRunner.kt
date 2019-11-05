package io.github.inoutch.kotchan.game.action.runner

interface ActionRunner {
    val id: Long

    val componentId: String

    fun interrupt()
}
