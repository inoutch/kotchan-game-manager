package io.github.inoutch.kotchan.game.test.util.action.task

import io.github.inoutch.kotchan.game.action.task.TaskStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1TaskStore(val customValue: String) : TaskStore() {
    override val factoryClass: String
        get() = className(Custom1TaskRunnerFactory::class)
}
