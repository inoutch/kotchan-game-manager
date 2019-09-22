package io.github.inoutch.kotchan.game.network

interface Network {

    fun send(packet: Packet)

    fun receive(packet: Packet)
}
