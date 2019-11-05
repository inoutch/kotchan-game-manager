package io.github.inoutch.kotchan.game.action.factory

import io.github.inoutch.kotchan.game.action.runner.TaskRunner

interface TaskRunnerFactory {
    fun create(): TaskRunner<*, *>
}
