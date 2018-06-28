package com.zskpaco.kotlinutils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import java.lang.ref.WeakReference

/**
 * Author: Richard paco
 * Date: 2018/6/28
 * Desc: 视图操作
 */

/**
 * 节流器
 */
interface ViewThrottle<T>

infix fun <T : View> T.throttle(sec: Float): ViewThrottle<T> = ViewThrottleImpl(WeakReference(this), sec, false)

infix fun <T> ViewThrottle<T>.clicks(listener: T.() -> Unit) {
    val impl = this as ViewThrottleImpl<T>
    impl.clicks(listener)
}

/**
 * 视图事件操作
 */
infix fun <T : View> T.clicks(event: T.() -> Unit) {
    this.setOnClickListener {
        event()
    }
}

infix fun <T : EditText> T.textChanges(event: String.() -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            event(s.toString())
        }
    })
}