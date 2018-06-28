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

private object BackgroundExecutor {
    private var executor: ExecutorService =
            Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

    fun <T> submit(task: () -> T): Future<T> = executor.submit(task)

}

object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
    val mainThread: Thread = Looper.getMainLooper().thread
}
