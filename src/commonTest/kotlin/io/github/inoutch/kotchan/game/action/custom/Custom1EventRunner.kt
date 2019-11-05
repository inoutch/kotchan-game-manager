package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.runner.EventRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1EventRunner : EventRunner<Custom1EventStore, CustomComponent>(Custom1EventStore::class, CustomComponent::class) {
    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun end() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(ratio: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun allowInterrupt(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun interrupt() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
