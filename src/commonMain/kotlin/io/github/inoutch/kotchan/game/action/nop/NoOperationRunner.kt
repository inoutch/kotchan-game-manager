package io.github.inoutch.kotchan.game.action.nop

import io.github.inoutch.kotchan.game.action.ActionManager.Companion.nopRunnerContextProvider
import io.github.inoutch.kotchan.game.action.ActionRunner

class NoOperationRunner : ActionRunner {
    override val id: Long
        get() = nopRunnerContextProvider.current.noOperationRuntimeStore.id

    override val componentId: String
        get() = nopRunnerContextProvider.current.noOperationRuntimeStore.componentId

    val store = nopRunnerContextProvider.current.noOperationRuntimeStore.store

    override fun start() {}

    override fun end() {}

    override fun interrupt() {}
}
