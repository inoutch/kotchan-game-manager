package io.github.inoutch.kotchan.game.test.util.action.task

import io.github.inoutch.kotchan.game.action.task.TaskStore
import io.github.inoutch.kotchan.game.extension.className

class Custom2TaskStore(val customValue: String, var count: Int) : TaskStore() {
    override val factoryClass: String
        get() = className(Custom2TaskRunnerFactory::class)
}
