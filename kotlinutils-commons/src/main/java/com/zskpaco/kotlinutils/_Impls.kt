package com.zskpaco.kotlinutils

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

/**
 * Author: Richard paco
 * Date: 2018/6/29
 * Desc: 实现类
 */

/**
 * 事件总线
 */
class FixSubscribeImpl<S : Any>(val type: KClass<S>, val subscribes: Subscriptions, var schedule: Schedulers) : FixSubscribe<S>


/**
 * 计时器
 */
class InternalImpl<T>(private val second: Float, val ref: WeakReference<T>) : Internal<T> {
    lateinit var next: Int.() -> Unit
    lateinit var start: T.() -> Unit
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
        start(ref.get()!!)
        handler.sendEmptyMessage(1)
    }
}

/**
 * View
 */
class ThrottleImpl<T>(private val ref: WeakReference<T>, private val sec: Float, private var status: Boolean) : Throttle<T> {
    fun clicks(listener: T.() -> Unit) {
        (ref.get()!! as View).setOnClickListener {
            event(listener)
        }
    }

    fun event(listener: T.() -> Unit) {
        if (!status) {
            status = true
            listener(ref.get()!!)
            ContextHelper.handler.postDelayed({
                status = false
            }, (sec * 1000).toLong())
        }
    }
}