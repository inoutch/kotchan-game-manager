package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.builder.EventBuilder
import io.github.inoutch.kotchan.game.action.builder.TaskBuilder
import io.github.inoutch.kotchan.game.action.runner.TaskRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1TaskRunner : TaskRunner<Custom1TaskStore, CustomComponent>(Custom1TaskStore::class, CustomComponent::class) {
    override fun next(builder: EventBuilder, interrupted: Boolean) {
    }

    override fun next(builder: TaskBuilder, interrupted: Boolean) {
    }

    override fun interrupt() {
    }
}
