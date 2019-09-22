package io.github.inoutch.kotchan.game.action

class ActionRunnerNode(
        val parent: ActionRunnerNode?,
        val children: List<ActionRunner>,
        val actionRunner: ActionRunner)
