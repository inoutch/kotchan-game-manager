package io.github.inoutch.kotchan.game.event

class EventBuilder {
    val eventStoreQueue: List<EventStore>
        get() = events

    private val events = mutableListOf<EventStore>()

    private var current: EventStore? = null

    fun enqueue(eventStore: EventStore) {
        if (current is EventReducerStore && eventStore is EventCreatorStore) {
            events.add(EventReducerStoreEnd())
        }

        events.add(eventStore)
    }
}
