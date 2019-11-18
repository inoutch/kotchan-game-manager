package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.store.TaskStore
import io.github.inoutch.kotchan.game.extension.className

class Custom2TaskStore(
    val taskName: String,
    val taskSize: Int,
    val eventSize: Int,
    val eventInterruptible: Boolean
) : TaskStore() {
    override val factoryClass = className(Custom2TaskRunnerFactory::class)

    var currentTaskSize = 0

    var currentEventSize = 0
}
