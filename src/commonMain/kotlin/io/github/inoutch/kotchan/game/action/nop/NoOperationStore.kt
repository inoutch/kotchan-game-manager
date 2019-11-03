package io.github.inoutch.kotchan.game.action.nop

import io.github.inoutch.kotchan.game.action.ActionStore
import io.github.inoutch.kotchan.game.action.event.EventStore
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class NoOperationStore(val type: NoOperationType) : ActionStore {
    override val factoryClass: String
        get() = className(NoOperationRunnerFactory::class)
}
