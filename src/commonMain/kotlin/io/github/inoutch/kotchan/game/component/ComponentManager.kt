package io.github.inoutch.kotchan.game.component

import io.github.inoutch.kotchan.game.extension.className
import io.github.inoutch.kotchan.game.component.group.ComponentGroup
import io.github.inoutch.kotchan.game.component.group.ComponentGroupByLabel
import io.github.inoutch.kotchan.game.extension.checkClass
import io.github.inoutch.kotchan.game.extension.filterIsInstance
import io.github.inoutch.kotchan.game.component.store.Store
import io.github.inoutch.kotchan.game.error.*
import io.github.inoutch.kotchan.game.util.ContextProvider
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.KClass

class ComponentManager {

    @ThreadLocal
    companion object {
        val componentManager = ComponentManager()

        val contextProvider = ContextProvider<ComponentContext>()
    }

    val componentSize: Int
        get() = components.size

    val waitingComponentSize: Int
        get() = waitingComponentStores.size

    val updatableComponentSize: Int
        get() = updatableComponents.size

    // id, components
    private val components = mutableMapOf<String, Component>()

    private val componentsByLifecycle = mutableMapOf<ComponentLifecycle, MutableList<Component>>()

    private val componentsByLabels = mutableMapOf<String, MutableList<Component>>()

    private val updatableComponents = mutableListOf<Component>()

    // For lazy load
    private var waitingComponentStores = mutableListOf<Store>()

    private var waitingComponents = mutableListOf<Component>()

    // factoryType, component factory
    private val factories = mutableMapOf<String, ComponentFactory>()

    // listeners
    private val listeners = mutableMapOf<KClass<*>, MutableList<ComponentLifecycleListener<Component>>>()

    private val invListeners = mutableMapOf<ComponentLifecycleListener<*>, KClass<Component>>()

    private val pureListeners = mutableListOf<ComponentLifecycleListener<Component>>()

    private var activated: (() -> Unit)? = null

    private var activatingSize = 10

    init {
        ComponentLifecycle.values().forEach { componentsByLifecycle[it] = mutableListOf() }
    }

    fun <T : Component> findById(id: String, componentClass: KClass<T>): ComponentAccessor<T>? {
        return components[id]?.checkClass(componentClass)?.let { ComponentAccessor.create(it) }
    }

    fun <T : Component> findByIdForLocal(id: String, componentClass: KClass<T>): T? {
        return components[id]?.checkClass(componentClass)
    }

    fun <T : Component> findByLabelForLocal(label: String, componentClass: KClass<T>): List<T> {
        return componentsByLabels[label]?.filterIsInstance(componentClass) ?: emptyList()
    }

    fun createComponent(store: Store): String? {
        val factoryType = store.factoryType
        checkNotNull(factoryType) { ERR_V_MSG_5 }

        val factory = factories[factoryType]
        checkNotNull(factory) { ERR_F_MSG_1(factoryType, factory) }

        return contextProvider.run(ComponentContext(store)) {
            val component = factory.create()
            this.attachComponent(component)
            component.id
        }
    }

    fun createComponent(store: Store, parentId: String): String? {
        store.replaceParentIdByComponentManager(parentId)
        return createComponent(store)
    }

    fun <T : Component> createComponentGroupByLabel(
            filterClass: KClass<T>,
            label: String,
            parentId: String = ""): ComponentAccessor<ComponentGroup<T>> {
        val components = componentsByLabels[label]
                ?.filterIsInstance(filterClass)
                ?.filter { it.lifecycle == ComponentLifecycle.UPDATE } ?: emptyList()

        return contextProvider.run(ComponentContext(Store().also { it.replaceParentIdByComponentManager(parentId) })) {
            val component = ComponentGroupByLabel(filterClass, label, components)
            this.attachComponent(component)
            ComponentAccessor.create(component)
        }
    }

    fun createComponentsIntermittently(
            stores: List<Store>,
            activated: () -> Unit = this.activated ?: throw IllegalStateException(ERR_V_MSG_4)) {
        this.activated = activated
        waitingComponentStores.addAll(stores.toMutableList())
    }

    fun activateComponent(id: String) {
        val store = waitingComponentStores.find { it.id == id }
        if (store != null) {
            waitingComponentStores.remove(store)
            createComponent(store)
            return
        }
        val component = waitingComponents.find { it.id == id } ?: return
        waitingComponents.remove(component)
        attachComponent(component)
    }

    fun activateAllComponents() {
        waitingComponents.forEach { attachComponent(it) }
        waitingComponents.clear()
    }

    fun registerComponentFactory(factory: ComponentFactory) {
        val factoryType = className(factory::class)
        if (this.factories[factoryType] != null) {
            throw Error("factory is already existed: factoryType is $factoryType")
        }
        factories[factoryType] = factory
    }

    fun unregisterComponentFactory(factoryClass: KClass<*>) {
        this.factories.remove(className(factoryClass))
    }

    fun unregisterAllComponentFactories() {
        this.factories.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> registerComponentListener(listener: ComponentLifecycleListener<T>, componentClass: KClass<T>) {
        this.listeners.getOrPut(componentClass) { mutableListOf() }.add(listener as ComponentLifecycleListener<Component>)
        this.invListeners[listener] = componentClass as KClass<Component>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> unregisterComponentListener(listener: ComponentLifecycleListener<T>) {
        this.listeners.remove(this.invListeners[listener] as KClass<Component>)
        this.invListeners.remove(listener)
        this.pureListeners.remove(listener as ComponentLifecycleListener<Component>)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> registerComponentListener(listener: ComponentLifecycleListener<T>) {
        this.pureListeners.add(listener as ComponentLifecycleListener<Component>)
    }

    fun update(delta: Float) {
        // 1. create
        this.updateCreateComponents()
        // 2. destroy
        this.updateWillDestroyComponents()
        // 3. update
        this.updateUpdateComponents(delta)
        // 4. activate
        this.updateWaitingComponents(delta)
    }

    fun <T : Component> reserveComponentDestruction(component: ComponentAccessor<T>) {
        component.access { l, c ->
            when (l) {
                ComponentLifecycle.WILL_DESTROY -> {
                }
                ComponentLifecycle.DESTROYED -> {
                }
                else -> reserveComponentDestruction(c)
            }
        }
    }

    fun destroyAllComponents() {
        this.changeLifecycle(ComponentLifecycle.WILL_DESTROY)
        this.updateWillDestroyComponents()
    }

    fun reserveComponentDestruction(component: Component) {
        component.lifecycle = ComponentLifecycle.WILL_DESTROY
        changeLifecycle(component, ComponentLifecycle.WILL_DESTROY)

        // Reserve destruction of child components
        this.componentsByLabels[component.id]?.forEach { reserveComponentDestruction(it) }
        component.willDestroy()
    }

    fun startToUpdate(component: Component) {
        if (component.lifecycle == ComponentLifecycle.UPDATE) {
            this.updatableComponents.add(component)
        }
    }

    fun stopToUpdate(component: Component) {
        if (component.lifecycle == ComponentLifecycle.UPDATE && !this.updatableComponents.contains(component)) {
            this.updatableComponents.remove(component)
        }
    }

    fun writeStores(): List<Store> {
        return components.map { it.value.store }
    }

    private fun <T : Component> reserveComponentDestruction(componentGroupAndComponents: ComponentGroup<T>) {
        componentGroupAndComponents.readOnlyComponents.forEach { x -> reserveComponentDestruction(x) }
    }

    private fun attachComponent(component: Component) {
        // Set lifecycle
        component.lifecycle = ComponentLifecycle.CREATE

        // Attach to core
        this.components[component.id] = component

        // Attach to labels
        val parentId = component.parentId
        if (parentId != null) {
            this.componentsByLabels
                    .getOrPut(parentId) { mutableListOf() }
                    .add(component)
        }
        component.labels.forEach {
            this.componentsByLabels.getOrPut(it) { mutableListOf() }.add(component)
        }

        // Attach to lifecycle
        this.componentsByLifecycle.getValue(component.lifecycle).add(component)
    }

    private fun detachComponent(component: Component) {
        // Detach from core
        this.components.remove(component.id)

        // Detach from labels
        this.componentsByLabels[component.id]?.remove(component)
        component.labels.forEach { this.componentsByLabels[it]?.remove(component) }

        // Detach from lifecycle
        val targets = this.componentsByLifecycle.getValue(component.lifecycle)
        targets.remove(component)

        // Notify detach component
        this.listeners[component::class]?.forEach { it.destroyed(component) }
        this.pureListeners.forEach { it.destroyed(component) }

        // Set lifecycle
        component.lifecycle = ComponentLifecycle.DESTROYED
    }

    private fun changeLifecycle(from: ComponentLifecycle, to: ComponentLifecycle) {
        val fromTargets = this.componentsByLifecycle.getValue(from)
        fromTargets.forEach { it.lifecycle = to }
        this.componentsByLifecycle[from] = mutableListOf()
        this.componentsByLifecycle.getValue(to).addAll(fromTargets)

        if (to == ComponentLifecycle.UPDATE) {
            this.updatableComponents.addAll(fromTargets.filter { it.updatable })
        } else if (from == ComponentLifecycle.UPDATE) {
            this.updatableComponents.removeAll(fromTargets)
        }
    }

    private fun changeLifecycle(component: Component, to: ComponentLifecycle) {
        val from = component.lifecycle
        this.componentsByLifecycle.getValue(component.lifecycle).remove(component)
        component.lifecycle = to
        this.componentsByLifecycle.getValue(to).add(component)

        if (!component.updatable) {
            return
        }
        if (to == ComponentLifecycle.UPDATE) {
            this.updatableComponents.add(component)
        } else if (from == ComponentLifecycle.UPDATE) {
            this.updatableComponents.remove(component)
        }
    }

    private fun changeLifecycle(label: String, to: ComponentLifecycle) {
        val targets = this.componentsByLabels[label] ?: return
        targets.forEach { changeLifecycle(it, to) }
    }

    private fun changeLifecycle(to: ComponentLifecycle) {
        ComponentLifecycle.values().forEach { this.componentsByLifecycle[it] = mutableListOf() }
        this.componentsByLifecycle.getValue(to).addAll(this.components.values)

        if (to == ComponentLifecycle.UPDATE) {
            this.updatableComponents.addAll(this.components.values)
        } else {
            this.updatableComponents.clear()
        }
    }

    private fun createWaitingComponent(store: Store): String? {
        val factoryType = store.factoryType
        checkNotNull(factoryType) { ERR_V_MSG_5 }

        val factory = factories[factoryType]
        checkNotNull(factory) { ERR_F_MSG_1(factoryType, factory) }

        return contextProvider.run(ComponentContext(store)) {
            val component = factory.create()
            component.lifecycle = ComponentLifecycle.WAIT
            waitingComponents.add(component)
            component.id
        }
    }

    private fun updateCreateComponents() {
        val targets = componentsByLifecycle.getValue(ComponentLifecycle.CREATE)
        if (targets.isEmpty()) {
            return
        }

        changeLifecycle(ComponentLifecycle.CREATE, ComponentLifecycle.INITIALIZE)

        for (target in targets) {
            try {
                target.create()

                // Notify created
                this.listeners[target::class]?.forEach { l -> l.create(target) }
                this.pureListeners.forEach { l -> l.create(target) }
            } catch (e: Error) {
                reserveComponentDestruction(target)
            }
        }

        changeLifecycle(ComponentLifecycle.INITIALIZE, ComponentLifecycle.UPDATE)
    }

    private fun updateUpdateComponents(delta: Float) {
        for (component in updatableComponents) {
            component.update(delta)
        }
    }

    private fun updateWillDestroyComponents() {
        val targets = componentsByLifecycle.getValue(ComponentLifecycle.WILL_DESTROY)
        if (targets.isEmpty()) {
            return
        }
        for (target in targets.toList()) {
            this.detachComponent(target)
        }
    }

    private fun updateWaitingComponents(delta: Float) {
        if (waitingComponentStores.isEmpty()) {
            return
        }

        if (delta <= 1.0f / 60.0f + 0.1) {
            activatingSize *= 2
        } else if (activatingSize > 1) {
            activatingSize /= 2
        }
        val size = min(waitingComponentStores.size, activatingSize)
        for (i in 0 until size) {
            createWaitingComponent(waitingComponentStores[i])
        }
        for (i in 0 until size) {
            waitingComponentStores.removeAt(0)
        }

        if (waitingComponentStores.isEmpty()) {
            activated?.invoke()
        }
    }
}
