package io.github.inoutch.kotchan.game.extension

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

open class Parent

class Child1 : Parent()

class Child2 : Parent()

abstract class Parent2

class Child3 : Parent2()

class Child4 : Parent2()

interface IParent

class Child5 : IParent

class Child6 : IParent

class AnyTest {
    @Test
    fun checkClass() {
        val a: Any = 1
        assertEquals(1, a.checkClass(Int::class))
        assertNull(a.checkClass(String::class))
    }

    @Test
    fun checkInheritance() {
        val c1 = Child1()

        assertSame(c1, c1.checkClass(Child1::class))
        assertSame(c1, c1.checkClass(Parent::class))
        assertNull(c1.checkClass(Child2::class))
    }

    @Test
    fun checkAbstract() {
        val c3 = Child3()

        assertSame(c3, c3.checkClass(Child3::class))
        assertSame(c3, c3.checkClass(Parent2::class))
        assertNull(c3.checkClass(Child4::class))
    }

    @Test
    fun checkInterface() {
        val c5 = Child5()

        assertSame(c5, c5.checkClass(Child5::class))
        assertSame(c5, c5.checkClass(IParent::class))
        assertNull(c5.checkClass(Child6::class))
    }
}
