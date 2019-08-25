package io.github.inoutch.kotchan.game.util

class ContextProvider<T> {
    val current: T
        get() = context ?: throw IllegalStateException("current is not set")

    private var context: T? = null

    fun <U> run(context: T, scope: () -> U): U {
        this.context = context
        try {
            val ret = scope()
            this.context = null
            return ret
        } catch (e: Error) {
            this.context = null
            throw e
        }
    }
}
