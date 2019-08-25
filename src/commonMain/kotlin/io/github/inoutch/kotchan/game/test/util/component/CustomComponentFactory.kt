package io.github.inoutch.kotchan.game.test.util.component

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentFactory
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class CustomComponentFactory : ComponentFactory {
    override fun create(): Component {
        return CustomComponent()
    }
}
