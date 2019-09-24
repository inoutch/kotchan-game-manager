package io.github.inoutch.kotchan.game.test.util.action.task

import io.github.inoutch.kotchan.game.action.ActionBuilder
import io.github.inoutch.kotchan.game.action.ActionRunner
import io.github.inoutch.kotchan.game.action.task.TaskRunner
import io.github.inoutch.kotchan.game.test.util.action.event.Custom1EventStore
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1TaskRunner : TaskRunner<Custom1TaskStore, CustomComponent>(Custom1TaskStore::class, CustomComponent::class) {

    override fun start() {
    }

    override fun end() {
    }

    override fun next(builder: ActionBuilder) {
        if (store.count-- <= 0) {
            return
        }
        builder.enqueue(Custom1EventStore("e1", 500))
        builder.enqueue(Custom1EventStore("e2", 500))
        builder.enqueue(Custom1EventStore("e3", 500))
    }

    override fun nextInterrupted(builder: ActionBuilder, caller: ActionRunner) {
    }

    override fun interrupt() {
    }
}
