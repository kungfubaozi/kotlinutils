package com.zskpaco.kotlinutils

import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

enum class Schedulers {
    immediate, ui, async
}

interface Subscribe<T>

interface FixSubscribe<T, S>

class FixSubscribeImpl<T, S : Any>(val type: KClass<S>, val subscribes: Subscribe<T>, var schedule: Schedulers) : FixSubscribe<T, S>

/**
 * 订阅
 */
inline infix fun <T, S : Any> Subscribe<T>.subscribe(type: KClass<S>): FixSubscribe<T, S> = FixSubscribeImpl(type, this, Schedulers.immediate)

/**
 * 粘性事件
 */
infix fun <T, S : Any> FixSubscribe<T, S>.sticky(observer: S.() -> Unit) {
    (this as FixSubscribeImpl).subscribes.subscribe(true, type, observer, schedule)
}

infix fun <T, S : Any> FixSubscribe<T, S>.observe(observer: S.() -> Unit) {
    (this as FixSubscribeImpl).subscribes.subscribe(false, type, observer, schedule)
}

/**
 * 选择模式
 */
infix fun <T, S : Any> FixSubscribe<T, S>.schedule(schedule: Schedulers): FixSubscribe<T, S> {
    (this as FixSubscribeImpl).schedule = schedule
    return this
}

/**
 * 订阅
 * 不能含有相同类型的subscribe,否则会被替换掉
 */
fun <T> T.subscriptions(subscribes: Subscribe<T>.() -> Unit): Subscribe<T> {
    val sub = SubscribeImpl(WeakReference(this), arrayListOf())
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

private fun <T, S : Any> Subscribe<T>.subscribe(isSticky: Boolean, eventType: KClass<S>, concrete: S.() -> Unit, scheduleMode: Schedulers) {
    val sub = this as SubscribeImpl<T>
    val formHost = sub.weakRef.get()!! as Any
    temp?.add(Subscription().apply {
        host = formHost
        type = eventType as KClass<Any>
        observer = concrete as Any.() -> Unit
        sticky = isSticky
        schedule = scheduleMode
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

private val currentPostingThreadState = object : ThreadLocal<PostingThreadState>() {
    override fun initialValue(): PostingThreadState {
        return PostingThreadState()
    }
}

private class PostingThreadState {
    val eventQueue: MutableList<Any> = ArrayList()
    var isPosting: Boolean = false
}