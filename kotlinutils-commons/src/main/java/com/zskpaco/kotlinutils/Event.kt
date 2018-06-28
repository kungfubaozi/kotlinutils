package com.zskpaco.kotlinutils

import android.app.Activity
import android.support.v4.app.Fragment
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

//观察者
class Observer<T : Any> {
    lateinit var data: T
    lateinit var type: KClass<T>
}

class FixObserver<T, S : Any>(val type: KClass<S>, val subscribes: Subscribe<T>)

interface Subscribe<T>

internal class AbsSubscribe<T>(val weakRef: WeakReference<T>) : Subscribe<T> {

    var temp: MutableList<Subscription>? = null
}

//具体订阅
@Deprecated("Use infix method")
fun <T, S : Any> Subscribe<T>.eventOf(type: KClass<S>, observer: Observer<S>.() -> Unit) {
    subscribe(false, type, observer)
}

//具体订阅
@Deprecated("Use infix method")
fun <T, S : Any> Subscribe<T>.stickyOf(type: KClass<S>, observer: Observer<S>.() -> Unit) {
    subscribe(true, type, observer)
}

fun <T, S : Any> Subscribe<T>.subscribe(type: KClass<S>): FixObserver<T, S> = FixObserver(type, this)

infix fun <T, S : Any> FixObserver<T, S>.sticky(observer: Observer<S>.() -> Unit) {
    subscribes.subscribe(true, type, observer)
}

infix fun <T, S : Any> FixObserver<T, S>.just(observer: Observer<S>.() -> Unit) {
    subscribes.subscribe(false, type, observer)
}

/**
 * 订阅
 * 不能含有相同类型的subscribe,否则会被替换掉
 */
fun <T> T.subscriptions(subscribes: Subscribe<T>.() -> Unit): Subscribe<T> {
    val sub = AbsSubscribe(WeakReference(this))
    sub.temp = arrayListOf()
    sub.subscribes()
    val fromHost = this as Any
    val kFromHost = fromHost::class as KClass<Any>
    //取消禁用
    EventBus.disabled.remove(kFromHost)
    var added = false
    if (!EventBus.registered.contains(kFromHost)) {
        // 没有数据
        sub.temp?.forEach { subscription ->
            if (!EventBus.subscriptions.containsKey(subscription.type)) {
                val subs: CopyOnWriteArrayList<Subscription> = CopyOnWriteArrayList()
                subs.add(subscription)
                EventBus.subscriptions[subscription.type] = subs
            } else {
                EventBus.subscriptions[subscription.type]?.add(subscription)
            }
        }
        added = true
    }

    // 更新数据
    sub.temp?.forEach { subscription ->
        if (!added) {
            EventBus.subscriptions[subscription.type]?.filter {
                it.hostName == subscription.hostName
            }?.forEach {
                it.observer = subscription.observer
                it.host = subscription.host
            }
        }
        // 粘性事件
        if (subscription.sticky) {
            EventBus.stickyEvents.remove(subscription.type)?.apply {
                subscription.call(this)
            }
        }
    }

    sub.temp = null

    return sub
}

//取消订阅
fun <T : Any> T.unsubscribe() {
    EventBus.disabled.add(this::class as KClass<Any>)
}

//发送事件
fun <T : Any> T.post(): T {
    postEvent(this, false)
    return this
}

fun <T : Any> T.postSticky(): T {
    postEvent(this, true)
    return this
}

fun <T : Any> Async<T>.post() {
    this.ref.get()?.post()
}

fun <T : Any> Async<T>.postSticky() {
    this.ref.get()?.postSticky()
}

private fun <T, S : Any> Subscribe<T>.subscribe(isSticky: Boolean, eventType: KClass<S>, concrete: Observer<S>.() -> Unit) {
    val sub = this as AbsSubscribe<T>
    val formHost = sub.weakRef.get()!! as Any
    temp?.add(Subscription().apply {
        host = formHost
        type = eventType as KClass<Any>
        observer = concrete as Observer<Any>.() -> Unit
        sticky = isSticky
        hostName = formHost.javaClass.canonicalName
    })
}

//发送事件
private fun postEvent(event: Any, sticky: Boolean = false) {

    if (sticky) {
        // 保存起来
        EventBus.stickyEvents[event::class as KClass<Any>] = event
    }

    val state = currentPostingThreadState.get()
    state.eventQueue.add(event)
    if (!state.isPosting) {
        state.isPosting = true
        try {
            while (!state.eventQueue.isEmpty()) {
                val eventObj = state.eventQueue.removeAt(0)
                EventBus.subscriptions.filterKeys {
                    eventObj::class == it
                }.forEach { _, value ->
                    value.forEach {
                        if (!EventBus.disabled.contains(it.type)) {
                            it.call(eventObj)
                        }
                    }
                }
            }
        } finally {
            state.isPosting = false
        }
    }

}

//事件
internal class Subscription {
    lateinit var hostName: String
    lateinit var host: Any
    lateinit var observer: Observer<Any>.() -> Unit
    lateinit var type: KClass<Any>
    var sticky = false

    fun call(event: Any) {
        if (host is Activity) {
            val activity = host as Activity
            if (!activity.isFinishing) {
                invoke(event)
            }
        } else if (host is Fragment) {
            val fragment = host as Fragment
            if (!fragment.isDetached) {
                invoke(event)
            }
        } else {
            invoke(event)
        }
    }

    private fun invoke(event: Any) {
        try {
            observer(Observer<Any>().apply {
                data = event
                type = this@Subscription.type
            })
        } catch (e: Exception) {
            crashLogger.invoke(e)
        }
    }
}

internal class EventBus {
    companion object {
        /**
         * 订阅的事件
         */
        val subscriptions: MutableMap<KClass<Any>, CopyOnWriteArrayList<Subscription>> = HashMap()

        /**
         * 粘性事件
         */
        val stickyEvents: MutableMap<KClass<Any>, Any> = HashMap()

        /**
         * 已经注册事件的host
         */
        val registered: MutableMap<KClass<Any>, MutableList<KClass<Any>>> = HashMap()

        /**
         * 被注销的
         */
        val disabled: HashSet<KClass<Any>> = hashSetOf()
    }
}

private val currentPostingThreadState = object : ThreadLocal<PostingThreadState>() {
    override fun initialValue(): PostingThreadState {
        return PostingThreadState()
    }
}

private class PostingThreadState {
    val eventQueue: MutableList<Any> = ArrayList()
    var isPosting: Boolean = false
}