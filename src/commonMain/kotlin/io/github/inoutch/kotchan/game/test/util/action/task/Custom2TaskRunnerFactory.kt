package io.github.inoutch.kotchan.game.test.util.action.task

import io.github.inoutch.kotchan.game.action.task.TaskRunner
import io.github.inoutch.kotchan.game.action.task.TaskRunnerFactory

class Custom2TaskRunnerFactory : TaskRunnerFactory {
    override fun create(): TaskRunner<*, *> {
        return Custom2TaskRunner()
    }
}
