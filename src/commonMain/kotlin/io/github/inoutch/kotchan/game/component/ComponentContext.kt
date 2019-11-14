package io.github.inoutch.kotchan.game.component

import io.github.inoutch.kotchan.game.component.store.Store
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_0

class ComponentContext(val store: Store) {
    inline fun <reified T> store(): T {
        val store = this.store
        if (store !is T) {
            throw Error(ERR_F_MSG_0(T::class, store::class))
        }
        return store
    }
}
