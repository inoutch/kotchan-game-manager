package io.github.inoutch.kotchan.game.component.store

import kotlinx.serialization.Serializable

@Serializable
abstract class StaticStore {
    abstract val id: String
}
