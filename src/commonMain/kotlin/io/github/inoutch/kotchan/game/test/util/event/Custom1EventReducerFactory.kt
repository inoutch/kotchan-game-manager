package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventReducer
import io.github.inoutch.kotchan.game.event.EventReducerFactory

class Custom1EventReducerFactory : EventReducerFactory {
    override fun create(): EventReducer<*, *> {
        return Custom1EventReducer()
    }
}
