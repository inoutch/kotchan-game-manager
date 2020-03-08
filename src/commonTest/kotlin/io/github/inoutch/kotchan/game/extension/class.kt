package io.github.inoutch.kotchan.game.extension

import kotlin.test.Test
import kotlin.test.assertEquals

class ClassTest {
    @Test
    fun checkClassName() {
        assertEquals("ClassTest", className(ClassTest::class))
    }
}