package io.github.inoutch.kotchan.game.event

interface EventReducerFactory {
    fun create(): EventReducer<*, *>
}
