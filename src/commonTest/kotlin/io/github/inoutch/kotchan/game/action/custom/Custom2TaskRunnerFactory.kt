package io.github.inoutch.kotchan.game.action.custom

import io.github.inoutch.kotchan.game.action.factory.TaskRunnerFactory
import io.github.inoutch.kotchan.game.action.runner.TaskRunner

class Custom2TaskRunnerFactory : TaskRunnerFactory {
    override fun create(): TaskRunner<*, *> {
        return Custom2TaskRunner()
    }
}
