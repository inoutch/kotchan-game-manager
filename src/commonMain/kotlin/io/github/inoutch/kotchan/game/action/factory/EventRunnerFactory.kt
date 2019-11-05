package io.github.inoutch.kotchan.game.action.factory

import io.github.inoutch.kotchan.game.action.runner.EventRunner

interface EventRunnerFactory {
    fun create(): EventRunner<*, *>
}
