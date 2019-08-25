package io.github.inoutch.kotchan.game.component.store

import kotlinx.serialization.Serializable

// CAUTION: DO NOT USE `type` PROPERTY FOR KOTLIN SERIALIZATION
@Serializable
open class Store {
    @Suppress("LeakingThis")
    open val factoryType: String = ""

    // TODO: Change hash algorithm
    val id: String
        get() = "component-${this.hashCode()}"

    // Update after created
    var parentId: String = ""
        private set

    open val labels: List<String>
        get() = emptyList()

    fun replaceParentIdByComponentManager(parentId: String) {
        this.parentId = parentId
    }
}
