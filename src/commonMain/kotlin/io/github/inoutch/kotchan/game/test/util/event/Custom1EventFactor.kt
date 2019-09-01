package io.github.inoutch.kotchan.game.test.util.event

import io.github.inoutch.kotchan.game.event.EventFactor
import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Serializable

@Serializable
class Custom1EventFactor : EventFactor() {
    override val durationTime: Long
        get() = 1000

    override val factoryClass: String
        get() = className(Custom1EventFactorRunnerFactory::class)
}
