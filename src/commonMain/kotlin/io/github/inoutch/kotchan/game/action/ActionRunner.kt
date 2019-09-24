package io.github.inoutch.kotchan.game.action

interface ActionRunner {
    val id: Long

    val componentId: String

    fun start()

    fun end()

    fun interrupt()
}
