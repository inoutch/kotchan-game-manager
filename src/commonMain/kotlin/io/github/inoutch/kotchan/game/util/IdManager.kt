package io.github.inoutch.kotchan.game.util

class IdManager {
    var nextId = 0L
        private set

    fun getAndIncrementNextId() = nextId++

    fun reset(id: Long = 0L) {
        nextId = id
    }
}
