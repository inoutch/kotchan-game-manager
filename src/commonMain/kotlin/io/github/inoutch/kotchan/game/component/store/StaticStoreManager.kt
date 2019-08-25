package io.github.inoutch.kotchan.game.component.store

import io.github.inoutch.kotchan.game.extension.className
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.native.concurrent.ThreadLocal

@Serializable
class RootStaticStore(val stores: List<StaticStoreWrapper>)

@Serializable
class StaticStoreWrapper(@Polymorphic val store: StaticStore)

class StaticStoreManager {
    @ThreadLocal
    companion object {
        val staticStoreManager = StaticStoreManager()
    }

    private lateinit var module: SerialModule
    private lateinit var json: Json

    private val stores = mutableMapOf<String, StaticStore>()

    val readonlyStores: Map<String, StaticStore>
        get() = stores

    fun init(builder: PolymorphicModuleBuilder<StaticStore>.() -> Unit) {
        module = SerializersModule {
            polymorphic<StaticStore> {
                StaticStore::class with StaticStore.serializer()
                builder(this)
            }
        }
        json = Json(configuration = JsonConfiguration.Stable.copy(strictMode = false), context = module)
    }

    fun load(jsonString: String) {
        val rootStore = json.parse(RootStaticStore.serializer(), jsonString)
        stores.putAll(rootStore.stores.map { it.store.id to it.store }.toMap())
    }

    fun add(staticStore: StaticStore) {
        if (stores.containsKey(staticStore.id)) {
            throw IllegalStateException("Static io.github.inoutch.kotchan.game.component.store id is already contained: id = ${staticStore.id}")
        }
        stores[staticStore.id] = staticStore
    }

    inline operator fun <reified T> get(id: String): T {
        val s = readonlyStores[id]
        if (s is T) {
            return s
        }
        val message = if (s == null) {
            "Static io.github.inoutch.kotchan.game.component.store is null: id = ${if (id.isBlank()) "'blank'" else id}"
        } else {
            "Static io.github.inoutch.kotchan.game.component.store is invalid type: id = ${if (id.isBlank()) "'blank'" else id}, expected type = ${className(T::class)}, actual type = ${className(s::class)}"
        }
        throw IllegalStateException(message)
    }
}
