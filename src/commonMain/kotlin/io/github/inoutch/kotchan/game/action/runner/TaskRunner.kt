package io.github.inoutch.kotchan.game.action.runner

import io.github.inoutch.kotchan.game.action.TaskManager.Companion.taskRunnerContextProvider
import io.github.inoutch.kotchan.game.action.builder.EventBuilder
import io.github.inoutch.kotchan.game.action.builder.TaskBuilder
import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.action.store.TaskStore
import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_3
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_5
import io.github.inoutch.kotchan.game.extension.checkClass
import kotlin.reflect.KClass

abstract class TaskRunner<T : TaskStore, U : Component>(
    taskClass: KClass<T>,
    componentClass: KClass<U>
) : ActionRunner {

    val runtimeStore = taskRunnerContextProvider.current.taskRuntimeStore

    val store = runtimeStore.taskStore.checkClass(taskClass)
            ?: throw IllegalStateException(ERR_F_MSG_5(taskClass, runtimeStore.taskStore::class))

    override val id = runtimeStore.id

    val component = componentManager.findById(runtimeStore.componentId, componentClass)
            ?: throw IllegalStateException(ERR_F_MSG_3(componentClass))

    override val componentId = component.raw.id

    abstract fun next(builder: EventBuilder, interrupted: Boolean)

    abstract fun next(builder: TaskBuilder, interrupted: Boolean)

    abstract fun start(eventRuntimeStore: EventRuntimeStore)

    abstract fun end(eventRuntimeStore: EventRuntimeStore)
}
