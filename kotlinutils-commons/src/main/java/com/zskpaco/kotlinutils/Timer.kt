package com.zskpaco.kotlinutils

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

/**
 * Author: Richard paco
 * Date: 2018/6/28
 * Desc: 时间操作类，转换等
 */

/**
 * 计时器
 */
interface Internal<T>

fun <T> T.interval(second: Float, internal: Internal<T>.() -> Unit): Internal<T> {
    val impl = InternalImpl<T>(second, WeakReference(this))
    impl.internal()
    impl.begin()
    return impl
}

inline fun <T> Internal<T>.next(noinline event: Int.() -> Unit) {
    (this as InternalImpl<T>).next = event
}

inline fun <T> Internal<T>.completed(noinline event: T.() -> Unit) {
    (this as InternalImpl<T>).completed = event
}

class InternalImpl<T>(private val second: Float, val ref: WeakReference<T>) : Internal<T> {
    lateinit var next: Int.() -> Unit
    lateinit var completed: T.() -> Unit

    fun begin() {
        val count = when {
            second >= 1f -> 1000
            second == 0f -> 0
            second < 0.1f -> 10
            else -> 100
        }
        var limit = (second * 1000 / count).toInt()
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                if (msg!!.what == 1) {
                    if (limit == 0) {
                        completed(ref.get()!!)
                    } else {
                        next(limit)
                        this.postDelayed({
                            limit--
                            sendEmptyMessage(1)
                        }, count.toLong())
                    }
                }
            }
        }
        handler.sendEmptyMessage(1)
    }
}
