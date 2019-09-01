package io.github.inoutch.kotchan.game.event

interface EventCreatorRunnerFactory {
    fun create(): EventCreatorRunner<*, *>
}
