package io.github.inoutch.kotchan.game.action

import io.github.inoutch.kotchan.game.action.event.*
import io.github.inoutch.kotchan.game.action.nop.*
import io.github.inoutch.kotchan.game.action.task.*
import io.github.inoutch.kotchan.game.error.*
import io.github.inoutch.kotchan.game.extension.className
import io.github.inoutch.kotchan.game.extension.fastForEach
import io.github.inoutch.kotchan.game.network.Mode
import io.github.inoutch.kotchan.game.network.NetworkInterface
import io.github.inoutch.kotchan.game.util.ContextProvider
import io.github.inoutch.kotchan.game.util.IdManager
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal

class ActionManager {
    @ThreadLocal
    companion object {
        val actionManager = ActionManager()

        val taskRunnerContextProvider = ContextProvider<TaskRunnerContext>()

        val eventRunnerContextProvider = ContextProvider<EventRunnerContext>()

        val nopRunnerContextProvider = ContextProvider<NoOperationRunnerContext>()
    }

    private val contexts = mutableMapOf<String, ActionComponentContext>() // Group by componentId

    private val nodeMap = mutableMapOf<Long, ActionNode>() // Registered all nodeMap

    private val runtimeActionIdManager = IdManager() // Incremental id manager

    private var time = 0L

    // Events
    private val eventRunnersSortedByStartTime = arrayListOf<EventRunner<*, *>>()

    private val eventRunnersSortedByEndTime = arrayListOf<EventRunner<*, *>>()

    private val updateEventRunners = mutableListOf<EventRunner<*, *>>()

    // Factories
    private val eventRunnerFactories = mutableMapOf<String, EventRunnerFactory>()

    private val taskRunnerFactories = mutableMapOf<String, TaskRunnerFactory>()

    // Network
    private var mode = Mode.Server

    private var networkInterface: NetworkInterface? = null

    // Serialization
    private lateinit var serializer: ProtoBuf

    // Counter
    val nodeSize: Int
        get() = nodeMap.size

    val contextSize: Int
        get() = contexts.size

    val runningEventSize: Int
        get() = eventRunnersSortedByEndTime.size

    val updatingEventSize: Int
        get() = updateEventRunners.size

    fun init(registerCallback: SerializersModuleBuilder.() -> Unit) {
        val module = SerializersModule(registerCallback)
        serializer = ProtoBuf(context = module)

        contexts.clear()
        nodeMap.clear()
        runtimeActionIdManager.reset()
        eventRunnersSortedByStartTime.clear()
        eventRunnersSortedByEndTime.clear()
        updateEventRunners.clear()

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

    fun update(delta: Float) {
        time += (delta * 1000.0f).toLong()

        endEventRunners()

        updateEventRunners()

        startEventRunners()
    }

    // Server only
    fun run(componentId: String, taskStore: TaskStore, endCallback: (() -> Unit)? = null) {
        // 現在実行中のTaskRunnerを確認
        check(mode == Mode.Server) { ERR_V_MSG_6 }
        check(!contexts.containsKey(componentId))

        // 対象のComponent用のContextを生成する
        val context = ActionComponentContext(ActionNode(componentId, taskStore, endCallback))
        contexts[componentId] = context

        run(context.current)
    }

    fun ratio(eventRuntimeStore: EventRuntimeStore): Float {
        return min(((time - eventRuntimeStore.startTime).toDouble() / eventRuntimeStore.eventStore.durationTime).toFloat(), 1.0f)
    }

    fun attachUpdatable(eventReducer: EventRunner<*, *>) {
        updateEventRunners.add(eventReducer)
    }

    fun detachUpdatable(eventReducer: EventRunner<*, *>) {
        updateEventRunners.remove(eventReducer)
    }

    fun interrupt(componentId: String) {
        val context = contexts[componentId]
        checkNotNull(context)

        // 現在実行中のActionRunnerを取得
        val runner = context.current.runner
        check(runner is EventRunner<*, *>)

        // 割り込みフラグを立てる
        context.current.interceptor = runner

        // 現在実行中のEventRunnerに割り込み許可の確認と通知をする
        val allowedInterrupt = runner.allowInterrupt()
        runner.interrupt()

        // 現在キューにあって実行中でないEventRunnerの割り込み処理と後始末をする
        var next = context.current.next
        while (next != null) {
            val nextRunner = next.runner
            checkNotNull(nextRunner)

            nextRunner.interrupt()
            nodeMap.remove(nextRunner.id)
            next = next.next
        }

        // 割り込みを許可する場合は即時親に実行を移す
        if (allowedInterrupt) {
            val parent = context.current.parent
            if (parent == null || run(parent)) {
                // 実行するものがない場合は終了通知と後始末をする
                contexts.remove(componentId)
                context.current.endCallback?.invoke()
            }
            return
        }

        // 割り込みが許可されない場合は実行中のEventRunnerを最後にする
        context.current.next = null
    }

    // Client only
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
            else -> throw IllegalStateException(ERR_V_MSG_1)
        }

        val parent = nodeMap[actionRuntimeStore.parentId]
        val actionNode = ActionNode(runner.componentId, store, null, parent, runner)

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

        nodeMap[runner.id] = actionNode
    }

    fun receiveInterrupt(id: Long) {
        TODO("refactor")
        val runner = nodeMap[id]?.runner
        checkNotNull(runner) { ERR_V_MSG_7 }

        val context = contexts[runner.componentId]
        checkNotNull(context) { ERR_V_MSG_7 }
        check(context.current.runner == runner) { ERR_V_MSG_7 }

        runner.interrupt()

        contexts.remove(runner.componentId)
    }

    fun receiveEnd(id: Long) {
        TODO("refactor")
        val runner = nodeMap[id]?.runner
        checkNotNull(runner) { ERR_V_MSG_7 }

        val context = contexts[runner.componentId]
        checkNotNull(context) { ERR_V_MSG_7 }
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
        nodeMap.remove(id)

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

    private fun run(node: ActionNode, startTime: Long = this.time, interrupt: ActionRunner? = null): Boolean {
        var currentInterrupt = interrupt
        val context = contexts[node.componentId]
        checkNotNull(context) { ERR_V_MSG_9 }

        // TaskRunnerのみ実行され、即時実行するTaskRunnerのNodeが格納される
        val nodeQueue = mutableListOf(node)

        // 階層構造化されたTaskRunnerとEventRunnerを生成＆実行する
        while (nodeQueue.isNotEmpty()) {
            // nodeQueueから１つの要素を取り除く
            val currentNode = nodeQueue.first()
            nodeQueue.removeAt(0)
            context.current = currentNode

            // TaskRunnerを実行して子のActionを生成する
            // contextQueueにあるActionNodeは必ずstoreのみ存在する
            val runner = currentNode.runner as TaskRunner<*, *>? ?: createTaskRunner(
                    node.componentId,
                    currentNode.store as TaskStore,
                    currentNode.parent?.runner as TaskRunner<*, *>?)
            val actionBuilder = ActionBuilder()
            // Runnerが生成された場合のみstartを実行する
            if (currentNode.runner == null) {
                runner.start()
            }
            runner.next(actionBuilder, currentInterrupt)

            // ActionNodeのrunnerは生成時nullであるため値をセットする
            currentNode.runner = runner
            nodeMap[runner.id] = currentNode

            // ActionRunnerが生成されなければ現在のTaskRunnerの実行を終了して次の処理に備える
            if (actionBuilder.actionStoreQueue.isEmpty()) {
                // TaskRunnerの終了通知
                networkInterface?.sendActionRunnerEnd(runner.id)
                runner.end()
                nodeMap.remove(runner.id)

                val next = currentNode.next
                if (next != null) {
                    // 同じ階層のActionがあれば次のTaskRunnerを実行する(元のActionRunnerはTaskRunner)
                    nodeQueue.add(next)
                    continue
                }

                val parent = currentNode.parent
                if (parent != null) {
                    // 親のTaskRunnerを実行する
                    if (currentInterrupt != null) {
                        currentInterrupt = runner
                    }
                    nodeQueue.add(parent)
                    continue
                }

                // このComponentに対するTaskRunnerの実行をやめる
                currentNode.endCallback?.invoke() // 終了通知
                contexts.remove(currentNode.componentId)
                return true
            }

            // 終了通知タスクの追加
            actionBuilder.enqueue(NoOperationStore(NoOperationType.End))

            // ActionNodeのchildrenを構造化する
            var prevNode: ActionNode? = null
            var i = 0
            while (i < actionBuilder.actionStoreQueue.size) {
                val store = actionBuilder.actionStoreQueue[i]
                val childNode = ActionNode(node.componentId, store, null, currentNode)
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
                    // EventのリストにEventRunnerを全て追加（nextがnullになるまで追加）
                    attachEventRunners(node.componentId, first, startTime)
                }
                is TaskStore -> {
                    // Taskの場合は最初のTaskのみ実行させる
                    nodeQueue.add(first)
                }
            }
        }

        return false
    }

    private fun updateEventRunners() {
        updateEventRunners.fastForEach { it.update(ratio(it.runtimeStore)) }
    }

    private fun createEventRunner(componentId: String, eventStore: EventStore, parentRunner: TaskRunner<*, *>?, startTime: Long): EventRunner<*, *> {
        return createEventRunner(EventRuntimeStore(
                componentId,
                runtimeActionIdManager.nextId(),
                parentRunner?.id ?: -1,
                startTime,
                eventStore))
    }

    private fun createTaskRunner(componentId: String, taskStore: TaskStore, parentRunner: TaskRunner<*, *>?): TaskRunner<*, *> {
        return createTaskRunner(TaskRuntimeStore(
                componentId,
                runtimeActionIdManager.nextId(),
                parentRunner?.id ?: -1,
                taskStore))
    }

    private fun createNoOperationRunner(componentId: String, noOperationStore: NoOperationStore, parentRunner: ActionRunner?): NoOperationRunner {
        return createNoOperationRunner(NoOperationRuntimeStore(
                runtimeActionIdManager.nextId(),
                componentId,
                parentRunner?.id ?: -1,
                noOperationStore))
    }

    private fun createEventRunner(eventRuntimeStore: EventRuntimeStore): EventRunner<*, *> {
        if (mode == Mode.Server) {
            networkInterface?.sendActionRuntimeStore(eventRuntimeStore)
        }
        val eventRunnerFactory = eventRunnerFactories[eventRuntimeStore.eventStore.factoryClass]
        checkNotNull(eventRunnerFactory) { ERR_F_MSG_2(eventRuntimeStore.eventStore.factoryClass, eventRunnerFactory) }

        return eventRunnerContextProvider.run(EventRunnerContext(eventRuntimeStore)) {
            eventRunnerFactory.create()
        }
    }

    private fun createNoOperationRunner(noOperationRuntimeStore: NoOperationRuntimeStore): NoOperationRunner {
        if (mode == Mode.Server) {
            networkInterface?.sendActionRuntimeStore(noOperationRuntimeStore)
        }
        return nopRunnerContextProvider.run(NoOperationRunnerContext(noOperationRuntimeStore)) {
            NoOperationRunner()
        }
    }

    private fun createTaskRunner(taskRuntimeStore: TaskRuntimeStore): TaskRunner<*, *> {
        if (mode == Mode.Server) {
            networkInterface?.sendActionRuntimeStore(taskRuntimeStore)
        }
        val taskRunnerFactory = taskRunnerFactories[taskRuntimeStore.taskStore.factoryClass]
        checkNotNull(taskRunnerFactory) { ERR_F_MSG_4(taskRuntimeStore.taskStore.factoryClass, taskRunnerFactory) }

        return taskRunnerContextProvider.run(TaskRunnerContext(taskRuntimeStore)) {
            taskRunnerFactory.create()
        }
    }

    private fun attachEventRunners(componentId: String, actionNode: ActionNode, time: Long) {
        var startTime = time
        var current = actionNode

        while (true) {
            // Nopの確認


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
            nodeMap[runner.id] = current

            startTime += runner.store.durationTime
            current = current.next ?: break
        }
        eventRunnersSortedByStartTime.sortBy { it.startTime }
    }

    private fun pullStartingEvents(): List<EventRunner<*, *>> {
        var index = 0
        val buffers = mutableListOf<EventRunner<*, *>>()

        while (index < eventRunnersSortedByStartTime.size) {
            val buffer = eventRunnersSortedByStartTime[index++]
            if (buffer.startTime > time) {
                break
            }
            buffers.add(buffer)
        }

        for (i in 0 until buffers.size) {
            eventRunnersSortedByStartTime.removeAt(0)
        }
        return buffers
    }

    private fun pullEndingEvents(): List<EventRunner<*, *>> {
        var index = 0
        val buffers = arrayListOf<EventRunner<*, *>>()

        while (index < eventRunnersSortedByEndTime.size) {
            val buffer = eventRunnersSortedByEndTime[index++]
            if (buffer.endTime > time) {
                break
            }
            buffers.add(buffer)
        }

        for (x in buffers) {
            eventRunnersSortedByEndTime.removeAt(0)
        }
        return buffers
    }

    private fun startEventRunners() {
        if (mode != Mode.Server) {
            return
        }

        val eventRunners = pullStartingEvents()
        if (eventRunners.isEmpty()) {
            return
        }

        for (eventRunner in eventRunners) {
            eventRunner.start()

            if (eventRunner.endTime <= time) {
                endEventRunner(eventRunner)
                continue
            }

            eventRunnersSortedByEndTime.add(eventRunner)
            if (eventRunner.updatable) {
                updateEventRunners.add(eventRunner)
            }
        }
        eventRunnersSortedByEndTime.sortBy { it.endTime }
    }

    private fun endEventRunners() {
        if (mode != Mode.Server) {
            return
        }

        val eventRunners = pullEndingEvents()
        if (eventRunners.isEmpty()) {
            return
        }

        for (eventRunner in eventRunners) {
            // EventRunnerの終了
            endEventRunner(eventRunner)
        }
    }

    private fun endEventRunner(runner: EventRunner<*, *>) {
        val node = nodeMap[runner.id]
        checkNotNull(node)

        runner.end()
        nodeMap.remove(runner.id)
        updateEventRunners.remove(node.runner)

        val context = contexts[node.componentId]
        checkNotNull(context) { ERR_V_MSG_9 }

        // 同じ階層にEventRunnerがある場合はそちらをcurrentにする
        val next = node.next
        if (next != null) {
            val nextRunner = next.runner
            if (nextRunner !is NoOperationRunner) {
                // 予約されたRunnerでない場合は実行を移す
                context.current = next
                return
            }

            when (nextRunner.store.type) {
                NoOperationType.End -> {
                    // 親がある場合は親のTaskRunnerを実行する
                    val parent = node.parent
                    if (parent == null || run(parent, runner.endTime)) {
                        node.endCallback?.invoke()
                        return
                    }
                }
                NoOperationType.Interrupt -> {
                    //
                    val parent = node.parent
                    if (parent == null || run(parent, runner.endTime, runner)) {
                        node.endCallback?.invoke()
                        return
                    }
                }
            }
        }

        throw IllegalStateException("Invalid event runner")
    }
}
