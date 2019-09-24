package io.github.inoutch.kotchan.game.action.task

import io.github.inoutch.kotchan.game.action.ActionBuilder
import io.github.inoutch.kotchan.game.action.ActionManager.Companion.taskRunnerContextProvider
import io.github.inoutch.kotchan.game.action.ActionRunner
import io.github.inoutch.kotchan.game.component.Component
import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_3
import kotlin.reflect.KClass

abstract class TaskRunner<T : TaskStore, U : Component>(
        taskClass: KClass<T>,
        componentClass: KClass<U>) : ActionRunner {

    val runtimeStore = taskRunnerContextProvider.current.taskRuntimeStore

    val store = runtimeStore.taskStore

    override val id = runtimeStore.id

    val component = componentManager.findById(runtimeStore.componentId, componentClass)
            ?: throw IllegalStateException(ERR_F_MSG_3(componentClass))

    override val componentId = component.raw.id

    abstract fun next(builder: ActionBuilder)

    abstract fun nextInterrupted(builder: ActionBuilder, caller: ActionRunner)
}
