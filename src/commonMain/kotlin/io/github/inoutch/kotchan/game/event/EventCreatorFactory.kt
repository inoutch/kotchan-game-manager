package io.github.inoutch.kotchan.game.event

interface EventCreatorFactory {
    fun create(): EventCreator<*, *>
}
