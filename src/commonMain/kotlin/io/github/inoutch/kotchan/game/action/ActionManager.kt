package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.event.*
import io.github.inoutch.kotchan.game.action.task.*
import io.github.inoutch.kotchan.game.extension.className
import io.github.inoutch.kotchan.game.network.NetworkInterface
import io.github.inoutch.kotchan.game.util.ContextProvider
import io.github.inoutch.kotchan.game.util.IdManager
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.native.concurrent.ThreadLocal

class ActionManager {
    @ThreadLocal
    companion object {
        val actionManager = ActionManager()

        val taskRunnerContextProvider = ContextProvider<TaskRunnerContext>()

        val eventRunnerContextProvider = ContextProvider<EventRunnerContext>()
    }

    private val contexts = mutableMapOf<String, ActionComponentContext>() // Group by componentId

    private val nodes = mutableMapOf<Long, ActionNode>() // Registered all nodes

    private val runtimeActionIdManager = IdManager() // Incremental id manager

    private var time = 0L

    // Events
    private val eventRunnersSortedByStartTime = arrayListOf<EventRunner<*, *>>()

    private val eventRunnersSortedByEndTime = arrayListOf<EventRunner<*, *>>()

    // Factories
    private val eventRunnerFactories = mutableMapOf<String, EventRunnerFactory>()

    private val taskRunnerFactories = mutableMapOf<String, TaskRunnerFactory>()

    // Network
    private var networkInterface: NetworkInterface? = null

    // Serialization
    private lateinit var serializer: ProtoBuf

    fun init(registerCallback: SerializersModuleBuilder.() -> Unit) {
        val module = SerializersModule(registerCallback)
        serializer = ProtoBuf(context = module)

        contexts.clear()
        nodes.clear()
        runtimeActionIdManager.reset()

        unregisterAllTaskRunnerFactories()
        unregisterAllEventRunnerFactories()
    }

    fun registerTaskRunnerFactory(taskRunnerFactory: TaskRunnerFactory) {
        taskRunnerFactories[className(taskRunnerFactory::class)] = taskRunnerFactory
    }

    fun registerEventRunnerFactory(eventRunnerFactory: EventRunnerFactory) {
        eventRunnerFactories[className(eventRunnerFactory::class)] = eventRunnerFactory
    }

    fun unregisterAllTaskRunnerFactories() {
        taskRunnerFactories.clear()
    }

    fun unregisterAllEventRunnerFactories() {
        eventRunnerFactories.clear()
    }

    fun receiveActionRuntimeStore(actionRuntimeStore: ActionRuntimeStore) {
        val store: ActionStore
        val runner = when (actionRuntimeStore) {
            is EventRuntimeStore -> {
                store = actionRuntimeStore.eventStore
                createEventRunner(actionRuntimeStore)
            }
            is TaskRuntimeStore -> {
                store = actionRuntimeStore.taskStore
                createTaskRunner(actionRuntimeStore)
            }
            else -> TODO()
        }

        val parent = nodes[actionRuntimeStore.parentId]
        val actionNode = ActionNode(store, parent, runner)

        if (parent != null) {
            // 親の要素に子を登録
            val children = parent.children
            if (children == null) {
                parent.children = actionNode
            } else {
                children.next = actionNode
            }
        } else {
            // 親がない場合はContextから作成
            contexts[runner.componentId] = ActionComponentContext(actionNode)
        }

        nodes[runner.id] = actionNode
    }

    fun receiveInterrupt(id: Long) {
        val runner = nodes[id]?.runner ?: TODO()
        val context = contexts[runner.componentId] ?: TODO()
        check(context.current.runner == runner)

        runner.interrupt()

        contexts.remove(runner.componentId)
    }

    fun receiveEnd(id: Long) {
        val runner = nodes[id]?.runner ?: TODO()
        val context = contexts[runner.componentId] ?: TODO()
        check(context.current.runner == runner)

        // 終了を通知
        when (runner) {
            is TaskRunner<*, *> -> {
                runner.end()
            }
            is EventRunner<*, *> -> {
                runner.end()
            }
        }
        nodes.remove(id)

        val next = context.current.next
        if (next != null) {
            // 同じ階層の次のActionをcurrentにする
            context.current = next
            return
        }

        val parent = context.current.parent
        if (parent == null) {
            contexts.remove(runner.componentId)
            return
        }

        // currentを親のActionに設定(サーバーと同期)
        context.current = parent
    }

    fun update(delta: Float) {
        time += (delta * 1000.0f).toLong()
    }

    fun run(componentId: String, taskStore: TaskStore): Boolean {
        // 現在実行中のTaskRunnerを確認
        val context = contexts[componentId]?.also {
            // 既にタスクが実行されている場合は強制割り込み
            networkInterface?.sendInterrupt(it.current.runner!!.id)

            // 割り込みを通知
            it.current.runner?.interrupt()
            contexts.remove(componentId)
        } ?: ActionComponentContext(ActionNode(taskStore), time)

        contexts[componentId] = context

        // TaskRunnerのみ入り、即時実行するTaskRunnerのNodeが格納される
        val contextQueue = mutableListOf(context.current)
        context.current.store = taskStore

        while (contextQueue.isNotEmpty()) {
            // contextQueueから１つの要素を取り除く
            val currentNode = contextQueue.first()
            contextQueue.removeAt(0)

            // TaskRunnerを実行してActionを生成する
            // contextQueueにあるActionNodeは必ずstoreのみ存在する(1)
            val runner = createTaskRunner(
                    componentId,
                    currentNode.store as TaskStore,
                    currentNode.parent?.runner as TaskRunner<*, *>?)
            val actionBuilder = ActionBuilder()
            runner.start()
            runner.next(actionBuilder)

            currentNode.runner = runner

            // Actionが生成されなければ現在のTaskRunnerを削除する
            if (actionBuilder.actionStoreQueue.isEmpty()) {
                // TaskRunnerの終了通知
                runner.end()

                val next = currentNode.next
                if (next != null) {
                    // 同じ階層の次のTaskRunnerを実行する
                    contextQueue.add(next)
                    continue
                }
                val parent = currentNode.parent
                if (parent != null) {
                    // 親のTaskRunnerをもう一度実行する
                    contextQueue.add(parent)
                    continue
                }
                // 引数から受け取ったTaskStoreのTaskRunnerの実行をやめる
                return false
            }

            // ActionNodeのchildrenを構造化する
            var prevNode: ActionNode? = null
            var i = 0
            while (i < actionBuilder.actionStoreQueue.size) {
                val store = actionBuilder.actionStoreQueue[i]
                val childNode = ActionNode(store, currentNode)
                if (prevNode == null) {
                    currentNode.children = childNode
                } else {
                    prevNode.next = childNode
                }
                prevNode = childNode
                i++
            }

            // Actionを生成後に最初の子がTaskかEventかを判別する(あるNodeの子はすべて同種であることが保証されている)
            val first = currentNode.children as ActionNode
            when (first.store) {
                is EventStore -> {
                    // EventのリストにRunnerを追加
                    attachEventRunnerToActionNode(componentId, first)
                }
                is TaskStore -> {
                    // Taskの場合は最初のTaskのみ実行させる
                    contextQueue.add(first)
                }
            }
        }

        return true
    }

    private fun createEventRunner(componentId: String, eventStore: EventStore, parentRunner: TaskRunner<*, *>?, startTime: Long): EventRunner<*, *> {
        return createEventRunner(EventRuntimeStore(
                componentId,
                runtimeActionIdManager.nextId(),
                startTime,
                parentRunner?.id ?: -1,
                eventStore))
    }

    private fun createTaskRunner(componentId: String, taskStore: TaskStore, parentRunner: TaskRunner<*, *>?): TaskRunner<*, *> {
        return createTaskRunner(TaskRuntimeStore(
                componentId,
                runtimeActionIdManager.nextId(),
                parentRunner?.id ?: -1,
                taskStore))
    }

    private fun createEventRunner(eventRuntimeStore: EventRuntimeStore): EventRunner<*, *> {
        val eventRunnerFactory = eventRunnerFactories[eventRuntimeStore.eventStore.factoryClass] ?: TODO()
        return eventRunnerContextProvider.run(EventRunnerContext(eventRuntimeStore)) {
            eventRunnerFactory.create()
        }
    }

    private fun createTaskRunner(taskRuntimeStore: TaskRuntimeStore): TaskRunner<*, *> {
        val taskRunnerFactory = taskRunnerFactories[taskRuntimeStore.taskStore.factoryClass] ?: TODO()
        return taskRunnerContextProvider.run(TaskRunnerContext(taskRuntimeStore)) {
            taskRunnerFactory.create()
        }
    }

    private fun attachEventRunnerToActionNode(componentId: String, actionNode: ActionNode) {
        var startTime = time
        var current = actionNode

        while (true) {
            // EventRunnerの作成
            val runner = createEventRunner(
                    componentId,
                    current.store as EventStore,
                    current.parent?.runner as TaskRunner<*, *>?,
                    startTime)

            // EventRunnerの実行準備
            eventRunnersSortedByStartTime.add(runner)

            // EventRunnerの登録
            current.runner = runner
            startTime += runner.store.durationTime
            current = current.next ?: break
        }
        eventRunnersSortedByStartTime.sortBy { it.runtimeStore.startTime }
    }
}
