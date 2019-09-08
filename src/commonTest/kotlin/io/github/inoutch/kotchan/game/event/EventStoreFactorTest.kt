package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.event.EventManager.Companion.eventManager
import io.github.inoutch.kotchan.game.test.util.event.Custom1EventReducerStore
import kotlin.test.*

class EventStoreFactorTest {
    @BeforeTest
    fun init() {
        eventManager.init {
            polymorphic(EventStore::class) {
                Custom1EventReducerStore::class with Custom1EventReducerStore.serializer()
            }
        }
    }

    @Test
    fun serialization() {
        val factor = Custom1EventReducerStore(500)
        val eventRuntime = EventRuntime(123, "test", factor, 456)

        val bytes = eventManager.dump(eventRuntime)
        val afterEventRuntime = eventManager.load(bytes)

        assertEquals(123, afterEventRuntime.id)
        assertEquals("test", eventRuntime.componentId)
        assertEquals(456, eventRuntime.startTime)
        assertNotEquals(factor, afterEventRuntime.eventStore)

        assertTrue { eventRuntime.eventStore is EventReducerStore }
        val afterFactor = eventRuntime.eventStore as EventReducerStore
        assertEquals(factor.durationTime, afterFactor.durationTime)
    }
}
