package io.github.inoutch.kotchan.game.test.util.action.event

import io.github.inoutch.kotchan.game.action.event.EventRunner
import io.github.inoutch.kotchan.game.action.event.EventRunnerFactory

class Custom1EventRunnerFactory : EventRunnerFactory {
    override fun create(): EventRunner<*, *> {
        return Custom1EventRunner()
    }
}
