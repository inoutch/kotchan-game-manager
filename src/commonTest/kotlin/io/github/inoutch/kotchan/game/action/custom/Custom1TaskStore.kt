package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.store.TaskStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1TaskStore(var count: Int) : TaskStore() {
    override val factoryClass = className(Custom1TaskRunnerFactory::class)
}
