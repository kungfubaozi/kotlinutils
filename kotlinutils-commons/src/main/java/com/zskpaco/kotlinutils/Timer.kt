package com.zskpaco.kotlinutils

import java.lang.ref.WeakReference

/**
 * Author: Richard paco
 * Date: 2018/6/28
 * Desc: 时间操作类，转换等
 */

/**
 * 计时器
 */
fun <T> T.interval(second: Float, internal: Internal<T>.() -> Unit): Internal<T> {
    val impl = InternalImpl<T>(second, WeakReference(this))
    impl.internal()
    impl.begin()
    return impl
}

/**
 * 计时器 开始计时
 */
inline fun <T> Internal<T>.start(noinline event: T.() -> Unit) {
    (this as InternalImpl<T>).start = event
}

/**
 * 计时器 计时时间回调
 */
inline fun <T> Internal<T>.next(noinline event: Int.() -> Unit) {
    (this as InternalImpl<T>).next = event
}

/**
 * 计时器 计时结束
 */
inline fun <T> Internal<T>.completed(noinline event: T.() -> Unit) {
    (this as InternalImpl<T>).completed = event
}

/**
 * 节流器
 */
inline fun <T> T.throttle(second: Float, noinline listener: T.() -> Unit) {
    val impl = ThrottleImpl<T>(WeakReference(this), second, false)
    impl.event(listener)
}