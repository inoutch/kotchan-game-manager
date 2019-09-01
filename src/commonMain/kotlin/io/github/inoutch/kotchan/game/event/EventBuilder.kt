package io.github.inoutch.kotchan.game.event

class EventBuilder {
    val eventQueue: List<Event>
        get() = events

    private val events = mutableListOf<Event>()

    private var current: Event? = null

    fun enqueue(event: Event) {
        if (current is EventFactor && event is EventCreator) {
            events.add(EventFactorEnd())
        }

        events.add(event)
    }
}
