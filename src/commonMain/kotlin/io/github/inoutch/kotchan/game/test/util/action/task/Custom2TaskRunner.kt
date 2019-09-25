package io.github.inoutch.kotchan.game.test.util.action.task

import io.github.inoutch.kotchan.game.action.ActionBuilder
import io.github.inoutch.kotchan.game.action.ActionRunner
import io.github.inoutch.kotchan.game.action.task.TaskRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom2TaskRunner : TaskRunner<Custom2TaskStore, CustomComponent>(Custom2TaskStore::class, CustomComponent::class) {
    override fun next(builder: ActionBuilder) {
        if (store.count-- <= 0) {
            return
        }

        builder.enqueue(Custom1TaskStore("${store.customValue}-t1", 1))
        builder.enqueue(Custom1TaskStore("${store.customValue}-t2", 1))
    }

    override fun nextInterrupted(builder: ActionBuilder, caller: ActionRunner) {
    }

    override fun start() {
        component.raw.states.add("${store.customValue}-t1-start")
    }

    override fun end() {
        component.raw.states.add("${store.customValue}-t1-end")
    }

    override fun interrupt() {
        component.raw.states.add("${store.customValue}-t1-interrupt")
    }
}
