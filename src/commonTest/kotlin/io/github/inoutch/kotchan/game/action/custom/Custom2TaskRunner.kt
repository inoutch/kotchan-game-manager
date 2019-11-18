package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.builder.EventBuilder
import io.github.inoutch.kotchan.game.action.builder.TaskBuilder
import io.github.inoutch.kotchan.game.action.runner.TaskRunner
import io.github.inoutch.kotchan.game.test.util.component.CustomComponent

class Custom2TaskRunner : TaskRunner<Custom2TaskStore, CustomComponent>(Custom2TaskStore::class, CustomComponent::class) {
    override fun next(builder: EventBuilder, interrupted: Boolean) {
        if (interrupted) {
            return
        }
        if (store.currentEventSize++ < store.eventSize) {
            builder.enqueue(Custom1EventStore(store.taskName, "e${store.currentEventSize}", store.eventInterruptible, 500))
        }
    }

    override fun next(builder: TaskBuilder, interrupted: Boolean) {
        if (interrupted) {
            return
        }
        if (store.currentTaskSize++ < store.taskSize) {
            builder.enqueue(Custom2TaskStore("${store.taskName}-t${store.currentTaskSize}", store.taskSize - store.currentTaskSize, store.eventSize, store.eventInterruptible))
            store.currentEventSize = 0
        }
    }
}
