package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.store.ActionRuntimeStore
import io.github.inoutch.kotchan.game.action.store.ActionStore
import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.action.store.EventStore
import io.github.inoutch.kotchan.game.action.store.TaskRuntimeStore
import io.github.inoutch.kotchan.game.action.store.TaskStore
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule

class SerialModule {
    companion object {
        fun generate(
            taskStoreRegister: PolymorphicModuleBuilder<TaskStore>.() -> Unit,
            eventStoreRegister: PolymorphicModuleBuilder<EventStore>.() -> Unit
        ): SerialModule {
            return SerializersModule {
                polymorphic(ActionStore::class) {
                    TaskStore::class with TaskStore.serializer()
                    EventStore::class with EventStore.serializer()
                }
                polymorphic(ActionRuntimeStore::class) {
                    TaskRuntimeStore::class with TaskRuntimeStore.serializer()
                    EventRuntimeStore::class with EventRuntimeStore.serializer()
                }
                polymorphic(TaskStore::class) {
                    @Suppress("UNCHECKED_CAST")
                    taskStoreRegister(this as PolymorphicModuleBuilder<TaskStore>)
                }
                polymorphic(EventStore::class) {
                    @Suppress("UNCHECKED_CAST")
                    eventStoreRegister(this as PolymorphicModuleBuilder<EventStore>)
                }
            }
        }
    }
}
