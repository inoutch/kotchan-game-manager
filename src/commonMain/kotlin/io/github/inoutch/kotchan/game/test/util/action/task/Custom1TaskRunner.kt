package io.github.inoutch.kotchan.game.test.util.action.task

import io.github.inoutch.kotchan.game.action.ActionBuilder
import io.github.inoutch.kotchan.game.action.task.TaskRunner
import io.github.inoutch.kotchan.game.test.util.action.event.Custom1EventStore
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1TaskRunner : TaskRunner<Custom1TaskStore, CustomComponent>(Custom1TaskStore::class, CustomComponent::class) {

    override fun next(builder: ActionBuilder) {
        if (store.count-- <= 0) {
            return
        }

        builder.enqueue(Custom1EventStore("${store.customValue}-ce1", 500))
        builder.enqueue(Custom1EventStore("${store.customValue}-ce2", 500))
        builder.enqueue(Custom1EventStore("${store.customValue}-ce3", 500))
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
