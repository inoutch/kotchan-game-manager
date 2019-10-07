package io.github.inoutch.kotchan.game.action

class ActionNode(
        val componentId: String,
        val store: ActionStore,
        val endCallback: (() -> Unit)? = null,
        var parent: ActionNode? = null,
        var runner: ActionRunner? = null,
        var children: ActionNode? = null,
        var next: ActionNode? = null,
        var interrupt: Boolean = false)
