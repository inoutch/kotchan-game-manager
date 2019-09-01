package io.github.inoutch.kotchan.game.component.store

import io.github.inoutch.kotchan.game.component.ComponentManager.Companion.componentManager
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.native.concurrent.ThreadLocal

@Serializable
class RootStore(val stores: List<StoreWrapper>)

// HACK: List does not have polymorphic serializer
@Serializable
class StoreWrapper(@Polymorphic val store: Store)

class StoreManager {
    @ThreadLocal
    companion object {
        val storeManager = StoreManager()
    }

    lateinit var module: SerialModule
        private set

    lateinit var json: Json
        private set

    fun init(builder: PolymorphicModuleBuilder<Store>.() -> Unit) {
        module = SerializersModule {
            polymorphic<Store> {
                Store::class with Store.serializer()
                builder(this)
            }
        }
        json = Json(configuration = JsonConfiguration.Stable.copy(strictMode = false), context = module)
    }

    fun store(): String {
        val stores = componentManager.writeStores()
                .filter { it.factoryType.isNotBlank() }

        return json.stringify(RootStore.serializer(), RootStore(stores.map { StoreWrapper(it) }))
    }

    fun restore(jsonString: String, autoInit: Boolean = true) {
        if (autoInit) {
            componentManager.destroyAllComponents()
        }

        val rootStore = json.parse(RootStore.serializer(), jsonString)
        rootStore.stores.forEach { componentManager.createComponent(it.store) }
    }

    fun restore(jsonString: String, autoInit: Boolean, activated: () -> Unit) {
        if (autoInit) {
            componentManager.destroyAllComponents()
        }

        val rootStore = json.parse(RootStore.serializer(), jsonString)
        componentManager.createComponentsIntermittently(rootStore.stores.map { it.store }, activated)
    }
}
