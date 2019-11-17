package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.builder.EventBuilder
import io.github.inoutch.kotchan.game.action.builder.TaskBuilder
import io.github.inoutch.kotchan.game.action.context.ActionComponentContext
import io.github.inoutch.kotchan.game.action.context.ActionComponentInitContext
import io.github.inoutch.kotchan.game.action.context.TaskRunnerContext
import io.github.inoutch.kotchan.game.action.factory.TaskRunnerFactory
import io.github.inoutch.kotchan.game.action.runner.TaskRunner
import io.github.inoutch.kotchan.game.action.store.EventRuntimeStore
import io.github.inoutch.kotchan.game.action.store.EventStore
import io.github.inoutch.kotchan.game.action.store.TaskRuntimeStore
import io.github.inoutch.kotchan.game.action.store.TaskStore
import io.github.inoutch.kotchan.game.error.ERR_F_MSG_4
import io.github.inoutch.kotchan.game.error.ERR_V_MSG_1
import io.github.inoutch.kotchan.game.error.ERR_V_MSG_6
import io.github.inoutch.kotchan.game.extension.className
import io.github.inoutch.kotchan.game.extension.fastForEach
import io.github.inoutch.kotchan.game.util.ContextProvider
import io.github.inoutch.kotchan.game.util.IdManager
import io.github.inoutch.kotchan.game.util.tree.SerializableNode
import io.github.inoutch.kotchan.game.util.tree.SerializableTree
import kotlin.native.concurrent.ThreadLocal
import kotlinx.serialization.Serializable

class TaskManager(val action: Action, initData: InitData = InitData.create()) {
    @ThreadLocal
    companion object {
        val taskRunnerContextProvider = ContextProvider<TaskRunnerContext>()
    }

    interface Listener {
        fun enqueue(eventRuntimeStore: EventRuntimeStore)

        fun interrupt(componentId: String)
    }

    interface Action {
        fun onEnd(componentId: String, rootTaskRunner: TaskRunner<*, *>)
    }

    @Serializable
    class InitData(
        val resetId: Long,
        val currentTime: Long,
        val eventSortedByStartTime: Array<EventRuntimeStore>,
        val eventsSortedByEndTime: Array<EventRuntimeStore>,
        val contexts: Map<String, ActionComponentInitContext>
    ) {
        companion object {
            fun create(): InitData {
                return InitData(0, 0, emptyArray(), emptyArray(), emptyMap())
            }
        }
    }

    val currentTime: Long
        get() = time

    private val listeners = mutableListOf<Listener>()

    private val factories = mutableMapOf<String, TaskRunnerFactory>()

    private val idManager = IdManager()

    private var time = initData.currentTime

    private val eventsSortedByStartTime = initData.eventSortedByStartTime.toCollection(ArrayList())

    private val eventsSortedByEndTime = initData.eventsSortedByEndTime.toCollection(ArrayList())

    // -- シリアライズ対象 --
    private val contexts = initData.contexts
            .map { it.key to ActionComponentContext.create(it.value) }
            .toMap()
            .toMutableMap()

    init {
        idManager.reset(initData.resetId)
    }

    fun addTaskListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeTaskListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun registerFactory(factory: TaskRunnerFactory) {
        factories[className(factory::class)] = factory
    }

    fun unregisterFactory() {
        factories.clear()
    }

    fun registerComponent(componentId: String) {
        check(!contexts.containsKey(componentId)) { ERR_V_MSG_1 }

        val tree = SerializableTree.create<TaskStore>()
        val context = ActionComponentContext(tree, mutableListOf(), -1)
        contexts[componentId] = context
    }

    fun unregisterComponent(componentId: String) {
        val context = contexts[componentId]
        checkNotNull(context) { ERR_V_MSG_6 }

        context.eventRuntimeStores.fastForEach {
            eventsSortedByStartTime.remove(it)
            eventsSortedByEndTime.remove(it)
        }
        contexts.remove(componentId)
    }

    fun run(componentId: String, taskStore: TaskStore) {
        val context = contexts[componentId]
        checkNotNull(context) { ERR_V_MSG_6 }

        val root = context.tree.add(taskStore)
        context.currentNodeId = root.id
        run(componentId, context, root)
    }

    fun interrupt(componentId: String) {
        val context = contexts[componentId]
        checkNotNull(context)

        context.interrupting = true
        if (context.eventRuntimeStores.first().eventStore.isInterruptible()) {
            // 直ぐに割り込み可能
            val current = context.tree[context.currentNodeId]
            checkNotNull(current)

            listeners.fastForEach { it.interrupt(componentId) }

            context.eventRuntimeStores.clear()

            run(componentId, context, current, this.time)
            return
        }

        // 直ぐに割り込みしない場合は通知しない
        val first = context.eventRuntimeStores.first()
        context.eventRuntimeStores.clear()
        context.eventRuntimeStores.add(first)
    }

    fun update(delta: Float) {
        time += (delta * 1000.0f).toLong()

        do {
            var running = endEventRuntimeStores()
            running += startEventRuntimeStores()
        } while (running > 0)
    }

    fun serialize(): InitData {
        return InitData(
                idManager.nextId,
                time,
                eventsSortedByStartTime.toTypedArray(),
                eventsSortedByEndTime.toTypedArray(),
                contexts.map { it.key to ActionComponentInitContext.create(it.value) }
                        .toMap())
    }

    private fun run(
        componentId: String,
        context: ActionComponentContext,
        root: SerializableNode<TaskStore>,
        startTime: Long = this.time
    ) {
        val queue = mutableListOf(root)

        while (queue.isNotEmpty()) {
            val current = queue.first()
            queue.removeAt(0)
            context.currentNodeId = current.id

            val runner = createTaskRunner(componentId, current.value)

            // Event作成フェーズ
            val eventBuilder = EventBuilder()
            runner.next(eventBuilder, context.interrupting)

            if (eventBuilder.actionStoreQueue.isNotEmpty()) {
                attachEventRunners(context, componentId, eventBuilder.actionStoreQueue, startTime)
                return
            }

            // Task作成フェーズ
            val taskBuilder = TaskBuilder()
            runner.next(taskBuilder, context.interrupting)

            if (taskBuilder.actionStoreQueue.isNotEmpty()) {
                var isFirst = true
                taskBuilder.actionStoreQueue.fastForEach {
                    val ret = context.tree.add(it, current.id)
                    if (isFirst) {
                        // Task実行を子に移す
                        queue.add(ret)
                        context.interrupting = false
                        isFirst = false
                    }
                }
                continue
            }

            val parent = context.tree[current.parentId]
            if (parent == null) {
                // 自身がrootであるため処理が終了
                action.onEnd(componentId, runner)
                return
            }

            context.tree.removeFromParent(parent.id, 0)
            val next = context.tree.get(parent.id, 0)
            if (next == null || context.interrupting) {
                queue.add(parent)
                context.tree.removeChildren(parent.id)
            } else {
                queue.add(next)
            }
        }
    }

    private fun startEventRuntimeStores(): Int {
        val eventRuntimeStores = pullStartingEvents()
        if (eventRuntimeStores.isEmpty()) {
            return 0
        }

        for (eventRuntimeStore in eventRuntimeStores) {
            listeners.fastForEach { it.enqueue(eventRuntimeStore) }

            if (eventRuntimeStore.endTime() <= time) {
                endEventRuntimeStore(eventRuntimeStore)
                continue
            }
            eventsSortedByEndTime.add(eventRuntimeStore)
        }
        return eventRuntimeStores.size
    }

    private fun endEventRuntimeStores(): Int {
        val eventRuntimeStores = pullEndingEvents()
        if (eventRuntimeStores.isEmpty()) {
            return 0
        }

        for (eventRuntimeStore in eventRuntimeStores) {
            // EventRunnerの終了
            endEventRuntimeStore(eventRuntimeStore)
        }
        return eventRuntimeStores.size
    }

    private fun createTaskRunner(componentId: String, taskStore: TaskStore): TaskRunner<*, *> {
        val taskRuntimeStore = TaskRuntimeStore(componentId, idManager.nextId(), taskStore)

        val taskRunnerFactory = factories[taskRuntimeStore.taskStore.factoryClass]
        checkNotNull(taskRunnerFactory) { ERR_F_MSG_4(taskRuntimeStore.taskStore.factoryClass, taskRunnerFactory) }

        return taskRunnerContextProvider.run(TaskRunnerContext(taskRuntimeStore)) {
            taskRunnerFactory.create()
        }
    }

    private fun createEventRuntimeStore(
        componentId: String,
        eventStore: EventStore,
        startTime: Long
    ): EventRuntimeStore {
        return EventRuntimeStore(componentId, idManager.nextId(), startTime, eventStore)
    }

    private fun attachEventRunners(
        context: ActionComponentContext,
        componentId: String,
        eventStores: List<EventStore>,
        time: Long
    ) {
        var startTime = time

        eventStores.fastForEach {
            val runtimeStore = createEventRuntimeStore(componentId, it, startTime)
            eventsSortedByStartTime.add(runtimeStore)
            context.eventRuntimeStores.add(runtimeStore)

            startTime += it.durationTime
        }
        eventsSortedByStartTime.sortBy { it.startTime }
    }

    private fun pullStartingEvents(): List<EventRuntimeStore> {
        var index = 0
        val buffers = mutableListOf<EventRuntimeStore>()

        while (index < eventsSortedByStartTime.size) {
            val buffer = eventsSortedByStartTime[index++]
            if (buffer.startTime > time) {
                break
            }
            buffers.add(buffer)
        }

        for (i in 0 until buffers.size) {
            eventsSortedByStartTime.removeAt(0)
        }
        return buffers
    }

    private fun pullEndingEvents(): List<EventRuntimeStore> {
        var index = 0
        val buffers = arrayListOf<EventRuntimeStore>()

        while (index < eventsSortedByEndTime.size) {
            val buffer = eventsSortedByEndTime[index++]
            if (buffer.endTime() > time) {
                break
            }
            buffers.add(buffer)
        }

        for (x in buffers) {
            eventsSortedByEndTime.removeAt(0)
        }
        return buffers
    }

    private fun endEventRuntimeStore(eventRuntimeStore: EventRuntimeStore) {
        val componentId = eventRuntimeStore.componentId
        val context = contexts.getValue(componentId)

        context.eventRuntimeStores.removeAt(0)
        if (context.eventRuntimeStores.isEmpty()) {
            // 実行すべきEventが無いのでTaskを実行
            val currentNode = context.tree[context.currentNodeId]
            checkNotNull(currentNode)

            run(componentId, context, currentNode, eventRuntimeStore.endTime())
        }
    }
}
