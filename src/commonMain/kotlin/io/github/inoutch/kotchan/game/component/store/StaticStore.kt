package io.github.inoutch.kotchan.game.component.store

import kotlinx.serialization.Serializable

@Serializable
open class StaticStore(val id: String) {
    companion object {
        fun <T> bridge(staticStore: StaticStore, scope: () -> T): T {
            actualStore = staticStore
            val ret = scope.invoke()
            actualStore = null
            return ret
        }
    }
}

private var actualStore: StaticStore? = null

val idealStore: StaticStore
    get() = actualStore ?: throw IllegalStateException("Static io.github.inoutch.kotchan.game.component.store is null, use bridge method for creation")

val staticStoreId: String
    get() = idealStore.id
