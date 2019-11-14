package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.store.TaskStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1TaskStore(val taskName: String, val taskSize: Int, val eventSize: Int) : TaskStore() {
    override val factoryClass = className(Custom1TaskRunnerFactory::class)

    var currentTaskSize = 0

    var currentEventSize = 0
}
