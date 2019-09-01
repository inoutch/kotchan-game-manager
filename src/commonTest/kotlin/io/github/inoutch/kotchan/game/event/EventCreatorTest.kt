package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.event.EventManager.Companion.eventManager
import io.github.inoutch.kotchan.game.test.util.event.Custom1EventCreator
import kotlin.test.*

class EventCreatorTest {
    @BeforeTest
    fun init() {
        eventManager.init {
            polymorphic(Event::class) {
                Custom1EventCreator::class with Custom1EventCreator.serializer()
            }
        }
    }

    @Test
    fun serialization() {
        val factory = Custom1EventCreator()
        val eventRuntime = EventRuntime(123, "test", factory, 456)

        val bytes = eventManager.dump(eventRuntime)
        val afterEventRuntime = eventManager.load(bytes)

        assertEquals(123, afterEventRuntime.id)
        assertEquals("test", afterEventRuntime.componentId)
        assertEquals(456, afterEventRuntime.startTime)
        assertNotEquals(eventRuntime.event, afterEventRuntime.event)

        assertTrue { afterEventRuntime.event is Custom1EventCreator }
    }
}
