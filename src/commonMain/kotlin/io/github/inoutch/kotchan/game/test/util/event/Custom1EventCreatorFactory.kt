package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventCreator
import io.github.inoutch.kotchan.game.event.EventCreatorFactory

class Custom1EventCreatorFactory : EventCreatorFactory {
    override fun create(): EventCreator<*, *> {
        return Custom1EventCreator()
    }
}
