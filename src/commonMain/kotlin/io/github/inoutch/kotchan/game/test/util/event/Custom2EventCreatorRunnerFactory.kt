package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventCreatorRunner
import io.github.inoutch.kotchan.game.event.EventCreatorRunnerFactory

class Custom2EventCreatorRunnerFactory : EventCreatorRunnerFactory {
    override fun create(): EventCreatorRunner<*, *> {
        return Custom2EventCreatorRunner()
    }
}
