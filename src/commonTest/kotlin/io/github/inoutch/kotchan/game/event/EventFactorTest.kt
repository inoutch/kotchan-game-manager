package io.github.inoutch.kotchan.game.event

import io.github.inoutch.kotchan.game.event.EventManager.Companion.eventManager
import io.github.inoutch.kotchan.game.test.util.event.Custom1EventFactor
import kotlin.test.*

class EventFactorTest {
    @BeforeTest
    fun init() {
        eventManager.init {
            polymorphic(Event::class) {
                Custom1EventFactor::class with Custom1EventFactor.serializer()
            }
        }
    }

    @Test
    fun serialization() {
        val factor = Custom1EventFactor()
        val eventRuntime = EventRuntime(123, "test", factor, 456)

        val bytes = eventManager.dump(eventRuntime)
        val afterEventRuntime = eventManager.load(bytes)

        assertEquals(123, afterEventRuntime.id)
        assertEquals("test", eventRuntime.componentId)
        assertEquals(456, eventRuntime.startTime)
        assertNotEquals(factor, afterEventRuntime.event)

        assertTrue { eventRuntime.event is EventFactor }
        val afterFactor = eventRuntime.event as EventFactor
        assertEquals(factor.durationTime, afterFactor.durationTime)
    }
}
