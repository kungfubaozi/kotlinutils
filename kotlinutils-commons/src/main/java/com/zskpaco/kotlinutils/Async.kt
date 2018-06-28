package com.zskpaco.kotlinutils

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

val crashLogger = { throwable: Throwable -> throwable.printStackTrace() }

class Async<T>(val ref: WeakReference<T>)

fun <T> Async<T>.uiThread(f: (T) -> Unit): Boolean {
    val ref = ref.get() ?: return false
    if (ContextHelper.mainThread == java.lang.Thread.currentThread()) {
        f(ref)
    } else {
        ContextHelper.handler.post { f(ref) }
    }
    return true
}

/**
 * 异步
 */
fun <T> T.doAsync(exception: ((Throwable) -> Unit)? = crashLogger, task: Async<T>.() -> Unit): Future<Unit> {
    val context = Async(WeakReference(this))
    return BackgroundExecutor.submit {
        try {
            context.task()
        } catch (e: Exception) {
            exception?.invoke(e)
            throw e
        }
    }
}

/**
 * 等待
 */
fun <T> T.wait(millis: Long = 500, task: T.() -> Unit) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        ContextHelper.handler.postDelayed({
            task()
        }, millis)
    } else {
        Thread.sleep(millis)
        task()
    }
}

fun <T> T.flow(task: Node<T>.() -> Unit): Flow<T> {
    val order = FlowImpl<T>()
    order.add(task, this)
    return order
}

fun <T> Flow<T>.node(task: Node<T>.() -> Unit): Flow<T> {
    (this as FlowImpl<T>).add(task)
    return this
}

fun <T> Flow<T>.node(check: Boolean, task: Node<T>.() -> Unit): Flow<T> {
    if (check) {
        return node(task)
    }
    return this
}


fun <T> Flow<T>.node(index: Int, task: Node<T>.() -> Unit): Flow<T> {

    return this
}

fun <T> Flow<T>.node(check: () -> Boolean, task: Node<T>.() -> Unit): Flow<T> {
    if (check()) {
        return node(task)
    }
    return this
}

fun <T> Flow<T>.start() {
    val impl = this as FlowImpl<T>
    val func = impl.list.removeAt(0)
    val next = object : Node<T> {
        override fun goto(node: Int) {
            goto(node, null)
        }

        override fun goto(node: Int, payload: Any?) {
        }

        override fun next() {
            if (!impl.list.isEmpty()) {
                start()
            }
        }
    }
    func.task(next)
}

interface Flow<T>
interface Node<T> {
    fun next()
    fun goto(node: Int)
    fun goto(node: Int, payload: Any?)
}

internal class FlowImpl<T> : Flow<T> {

    val list: MutableList<NodeFunc<T>> = arrayListOf()

    internal class NodeFunc<T> {
        lateinit var task: Node<T>.() -> Unit
        var obj: Any? = null
    }

    fun add(task: Node<T>.() -> Unit, host: T? = null) {
        val func = NodeFunc<T>()
        func.obj = host
        func.task = task
        list.add(func)
    }

}

private object BackgroundExecutor {
    private var executor: ExecutorService =
            Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

    fun <T> submit(task: () -> T): Future<T> = executor.submit(task)

}

private object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
    val mainThread: Thread = Looper.getMainLooper().thread
}
