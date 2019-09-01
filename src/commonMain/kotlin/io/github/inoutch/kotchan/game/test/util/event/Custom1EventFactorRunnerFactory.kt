package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventFactorRunner
import io.github.inoutch.kotchan.game.event.EventFactorRunnerFactory

class Custom1EventFactorRunnerFactory : EventFactorRunnerFactory {
    override fun create(): EventFactorRunner<*, *> {
        return Custom1EventFactorRunner()
    }
}
