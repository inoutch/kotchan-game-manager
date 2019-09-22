package io.github.inoutch.kotchan.game.test.util.action.task

import io.github.inoutch.kotchan.game.action.task.TaskRunner
import io.github.inoutch.kotchan.game.action.task.TaskRunnerFactory

class Custom1TaskRunnerFactory : TaskRunnerFactory {
    override fun create(): TaskRunner<*, *> {
        return Custom1TaskRunner()
    }
}
