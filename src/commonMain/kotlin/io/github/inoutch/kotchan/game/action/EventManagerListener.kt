package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore

interface EventManagerListener {

    fun start(eventRuntimeStore: EventRuntimeStore)

    fun end(eventRuntimeStore: EventRuntimeStore)
}
