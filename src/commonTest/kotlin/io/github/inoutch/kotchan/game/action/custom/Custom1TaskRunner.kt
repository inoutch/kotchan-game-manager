package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.builder.EventBuilder
import io.github.inoutch.kotchan.game.action.builder.TaskBuilder
import io.github.inoutch.kotchan.game.action.runner.TaskRunner
import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom1TaskRunner : TaskRunner<Custom1TaskStore, CustomComponent>(Custom1TaskStore::class, CustomComponent::class) {
    override fun next(builder: EventBuilder, interrupted: Boolean) {
        if (store.currentEventSize++ < store.eventSize) {
            builder.enqueue(Custom1EventStore(store.taskName, "e${store.currentEventSize}", store.eventInterruptible, 500))
        }
    }

    override fun next(builder: TaskBuilder, interrupted: Boolean) {
        if (store.currentTaskSize++ < store.taskSize) {
            builder.enqueue(Custom1TaskStore("${store.taskName}-t${store.currentTaskSize}", 0, store.eventSize, store.eventInterruptible))
            store.currentEventSize = 0
        }
    }

    override fun start(eventRuntimeStore: EventRuntimeStore) {}

    override fun end(eventRuntimeStore: EventRuntimeStore) {}
}
