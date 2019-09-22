package io.github.inoutch.kotchan.game.network

interface NetworkInterface {
    fun send(data: ByteArray)

    fun sendInterrupt(id: Long)
}
