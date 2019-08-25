package io.github.inoutch.kotchan.game.test.util.component

import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentFactory

class CustomChildComponentFactory : ComponentFactory {
    override fun create(): Component {
        return CustomChildComponent()
    }
}
