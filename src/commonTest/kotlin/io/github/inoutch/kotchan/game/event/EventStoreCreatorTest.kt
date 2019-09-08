package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.event.EventManager.Companion.eventManager
import io.github.inoutch.kotchan.game.test.util.event.Custom1EventCreatorStore
import kotlin.test.*

class EventStoreCreatorTest {
    @BeforeTest
    fun init() {
        eventManager.init {
            polymorphic(EventStore::class) {
                Custom1EventCreatorStore::class with Custom1EventCreatorStore.serializer()
            }
        }
    }

    @Test
    fun serialization() {
        val factory = Custom1EventCreatorStore()
        val eventRuntime = EventRuntime(123, "test", factory, 456)

        val bytes = eventManager.dump(eventRuntime)
        val afterEventRuntime = eventManager.load(bytes)

        assertEquals(123, afterEventRuntime.id)
        assertEquals("test", afterEventRuntime.componentId)
        assertEquals(456, afterEventRuntime.startTime)
        assertNotEquals(eventRuntime.eventStore, afterEventRuntime.eventStore)

        assertTrue { afterEventRuntime.eventStore is Custom1EventCreatorStore }
    }
}
