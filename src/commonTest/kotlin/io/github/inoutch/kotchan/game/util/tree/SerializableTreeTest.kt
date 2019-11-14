package io.github.inoutch.kotchan.game.util.tree

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.protobuf.ProtoBuf

class SerializableTreeTest {
    @Test
    fun checkStandard() {
        val tree = SerializableTree.create<String>()
//        A
//        ├ B
//        │ └ C
//        ├ D
//        │ ├ E
//        │ └ F
//        │ 　 └ G
//        └ H
        val a = tree.add("A")
        val b = tree.add("B", a.id)
        tree.add("C", b.id)
        val d = tree.add("D", a.id)
        tree.add("E", d.id)
        val f = tree.add("F", d.id)
        tree.add("G", f.id)
        tree.add("H", a.id)

        assertEquals(8, tree.size)

        // get by id
        assertEquals("A", tree[a.id]?.value)
        assertEquals("B", tree[b.id]?.value)

        // get by child id
        assertEquals("D", tree.get(a.id, 1)?.value)
        assertEquals("E", tree.get(d.id, 0)?.value)
    }

    @Test
    fun checkRelease() {
        val tree = SerializableTree.create<String>()
//        A
//        ├ B
//        │ └ C
//        ├ D
//        │ ├ E
//        │ └ F
//        │ 　 └ G
//        └ H
        val a = tree.add("A")
        val b = tree.add("B", a.id)
        tree.add("C", b.id)
        val d = tree.add("D", a.id)
        tree.add("E", d.id)
        val f = tree.add("F", d.id)
        tree.add("G", f.id)
        tree.add("H", a.id)

        tree.remove(f.id)
        assertEquals(6, tree.size)

        tree.remove(d.id)
        assertEquals(4, tree.size)

        tree.remove(a.id)
        assertEquals(0, tree.size)

        assertEquals(0, tree.toArray().size)
    }

    @Test
    fun checkSerialization() {
        val tree = SerializableTree.create<String>()
//        A
//        ├ B
//        │ └ C
//        ├ D
//        │ ├ E
//        │ └ F
//        │ 　 └ G
//        └ H
        val a = tree.add("A")
        val b = tree.add("B", a.id)
        tree.add("C", b.id)
        val d = tree.add("D", a.id)
        tree.add("E", d.id)
        val f = tree.add("F", d.id)
        tree.add("G", f.id)
        tree.add("H", a.id)

        val root = Root(tree.toArray())
        val protoBuf = ProtoBuf()
        val bytes = protoBuf.dump(Root.serializer(), root)
        val restoredRoot = protoBuf.load(Root.serializer(), bytes)
        val restoredTree = SerializableTree.create(restoredRoot.array)

        assertEquals(8, restoredTree.size)
    }
}
