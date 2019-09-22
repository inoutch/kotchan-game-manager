package io.github.inoutch.kotchan.game.action

class ActionNode(
        var store: ActionStore,
        var parent: ActionNode? = null,
        var runner: ActionRunner? = null,
        var children: ActionNode? = null,
        var next: ActionNode? = null)
