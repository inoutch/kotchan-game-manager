package io.github.inoutch.kotchan.game.util

class IdManager {
    private var nextId = 0L

    fun nextId() = nextId++
}
