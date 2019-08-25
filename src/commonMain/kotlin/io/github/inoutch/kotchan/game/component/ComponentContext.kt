package io.github.inoutch.kotchan.game.component

import io.github.inoutch.kotchan.game.component.store.Store
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_0
import io.github.inoutch.kotchan.game.extension.checkClass
import kotlin.reflect.KClass

class ComponentContext(val store: Store) {
    inline fun <reified T> store(): T {
        val store = this.store
        if (store !is T) {
            throw Error(ERR_F_MSG_0(T::class, store::class))
        }
        return store
    }
}
