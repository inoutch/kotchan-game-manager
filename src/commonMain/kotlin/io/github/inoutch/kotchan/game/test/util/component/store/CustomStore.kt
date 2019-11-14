package io.github.inoutch.kotchan.game.test.util.component.store

import io.github.inoutch.kotchan.game.component.store.Store
import io.github.inoutch.kotchan.game.extension.className
import io.github.inoutch.kotchan.game.test.util.component.CustomComponentFactory
import kotlinx.serialization.Serializable

@Serializable
class CustomStore(val customValue: String) : Store() {
    override val factoryType: String
        get() = className(CustomComponentFactory::class)
}
