package io.github.inoutch.kotchan.game.serialization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

class SerializationTest {
    @Test
    fun polymorphic() {
        @Serializable
        abstract class A {
            abstract val a: Int
            abstract fun call(): String
        }

        @Serializable
        class B(override val a: Int, val b: Int) : A() {
            override fun call() = "B"
        }

        @Serializable
        class C(override val a: Int, val c: Int) : A() {
            override fun call() = "C"
        }

        @Serializable
        data class AWrapper(val a: A)

        val module = SerializersModule {
            polymorphic(A::class) {
                B::class with B.serializer()
                C::class with C.serializer()
            }
        }

        val json = Json(context = module)
        val b = json.stringify(AWrapper.serializer(), AWrapper(B(1, 2)))
        val c = json.stringify(AWrapper.serializer(), AWrapper(C(3, 4)))

        assertEquals(json.parse(AWrapper.serializer(), b).a.call(), "B")
        assertEquals(json.parse(AWrapper.serializer(), c).a.call(), "C")
    }

    @Test
    fun polymorphicDefaultParams() {
        @Serializable
        abstract class A {
            abstract val a: Int
            open val aa: Int = 0
        }

        @Serializable
        class B(override val a: Int = 1, val b: Int = 2) : A() {
            override val aa: Int
                get() = 33
        }

        @Serializable
        class C(override val a: Int = 3, val c: Int = 4) : A()

        @Serializable
        data class AWrapper(val a: A)

        val module = SerializersModule {
            polymorphic(A::class) {
                B::class with B.serializer()
                C::class with C.serializer()
            }
        }

        val json = Json(context = module)
        val b = json.stringify(AWrapper.serializer(), AWrapper(B(1, 2)))
        val c = json.stringify(AWrapper.serializer(), AWrapper(C(3, 4)))

        assertEquals(json.parse(AWrapper.serializer(), b).a.a, 1)
        assertEquals(json.parse(AWrapper.serializer(), c).a.a, 3)
        assertEquals(json.parse(AWrapper.serializer(), b).a.aa, 33)
    }
}
