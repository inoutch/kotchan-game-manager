package io.github.inoutch.kotchan.game.action.task

interface TaskRunnerFactory {
    fun create(): TaskRunner<*, *>
}
