package io.github.inoutch.kotchan.game.action.event

interface EventRunnerFactory {
    fun create(): EventRunner<*, *>
}
