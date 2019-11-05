package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.factory.EventRunnerFactory
import io.github.inoutch.kotchan.game.action.runner.EventRunner

class Custom1EventRunnerFactory : EventRunnerFactory {
    override fun create(): EventRunner<*, *> {
        return Custom1EventRunner()
    }
}
