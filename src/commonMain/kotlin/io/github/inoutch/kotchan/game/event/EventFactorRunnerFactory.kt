package io.github.inoutch.kotchan.game.event

interface EventFactorRunnerFactory {
    fun create(): EventFactorRunner<*, *>
}
