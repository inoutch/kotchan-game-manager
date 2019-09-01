package io.github.inoutch.kotchan.game.test.util.component

import io.github.inoutch.kotchan.game.component.Component

class CustomChildComponent : Component() {
    lateinit var component: CustomComponent

    override fun create() {
        super.create()
        component = parent()
    }
}
