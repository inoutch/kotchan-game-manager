package io.github.inoutch.kotchan.game.network

import io.github.inoutch.kotchan.game.action.ActionRuntimeStore

interface NetworkInterface {

    fun sendActionRuntimeStore(actionRuntimeStore: ActionRuntimeStore)

    fun sendActionRunnerEnd(id: Long)

    fun sendInterrupt(id: Long)
}
