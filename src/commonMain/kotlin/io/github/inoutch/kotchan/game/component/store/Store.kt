package io.github.inoutch.kotchan.game.component.store

import kotlinx.serialization.Serializable

// CAUTION: DO NOT USE `type` PROPERTY FOR KOTLIN SERIALIZATION
@Serializable
open class Store {
    open val factoryType: String
        get() = ""

    // TODO: Change hash algorithm
    open val id: String
        get() = "cp-${this.hashCode()}"

    // Update after created
    var parentId: String = ""
        private set

    open val labels: List<String>
        get() = emptyList()

    fun replaceParentIdByComponentManager(parentId: String) {
        this.parentId = parentId
    }
}
