package com.zskpaco.kotlinutils

import android.app.Activity
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.app.Fragment
import android.view.View
import java.io.*
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.reflect.KClass

/**
 * Author: Richard paco
 * Date: 2018/6/28
 * Desc: 内部构件
 */

/**
 * SharedPreferences
 */
internal class SharedPreferencesOperator : PreferencesOperate, Preferences {
    override fun delete(key: String) {
        editor.remove(key)
    }

    companion object {
        lateinit var instance: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
    }

    override fun putSerializable(key: String, value: Serializable) {
        try {
            val baos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(baos)
            oos.writeObject(value)
            val string64 = String(android.util.Base64.encode(baos.toByteArray(), 0))
            logi("baseencode= $string64")
            SharedPreferencesOperator.editor.putString(key, string64).commit()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun <T : Serializable> getSerializable(key: String): T? {
        var obj: T? = null
        try {
            val base64 = instance.getString(key, "")
            if (base64 == "") {
                return null
            }
            val base64Bytes = android.util.Base64.decode(base64.toByteArray(), 1)
            val bais = ByteArrayInputStream(base64Bytes)
            val ois = ObjectInputStream(bais)
            obj = ois.readObject() as T?
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return obj
    }

    override fun getBoolean(key: String): Boolean = instance.getBoolean(key, false)

    override fun getString(key: String): String = instance.getString(key, null)

    override fun getInt(key: String): Int = instance.getInt(key, 0)

    override fun getFloat(key: String): Float = instance.getFloat(key, 0f)

    override fun getLong(key: String): Long = instance.getLong(key, 0)

    override fun commit() {
        editor.commit()
    }

    override fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }

    override fun putString(key: String, value: String) {
        editor.putString(key, value)
    }

    override fun putInt(key: String, value: Int) {
        editor.putInt(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        editor.putFloat(key, value)
    }

    override fun putLong(key: String, value: Long) {
        editor.putLong(key, value)
    }
}

/**
 * Async
 */
internal object BackgroundExecutor {
    private var executor: ExecutorService =
            Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

    fun <T> submit(task: () -> T): Future<T> = executor.submit(task)

}

internal object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
    val mainThread: Thread = Looper.getMainLooper().thread
}

/**
 * EventBus
 */
internal class SubscribeImpl<T>(val weakRef: WeakReference<T>, var temp: MutableList<Subscription>?) : Subscribe<T>

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

internal class Subscription {
    lateinit var hostName: String
    lateinit var host: Any
    lateinit var schedule: Schedulers
    lateinit var observer: Any.() -> Unit
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
        fun call() {
            try {
                observer(event)
            } catch (e: Exception) {
                crashLogger.invoke(e)
            }
        }

        when (schedule) {
            Schedulers.immediate -> call()
            Schedulers.async -> {
                doAsync {
                    call()
                }
            }
            Schedulers.ui -> {
                ContextHelper.handler.post { call() }
            }
        }
    }
}

/**
 * View
 */
internal class ViewThrottleImpl<T>(private val ref: WeakReference<T>, val sec: Float, private var status: Boolean) : ViewThrottle<T> {
    fun clicks(listener: T.() -> Unit) {
        val view = ref.get()!! as View
        view.setOnClickListener {
            if (!status) {
                status = true
                listener(view as T)
                ContextHelper.handler.postDelayed({
                    status = false
                }, (sec * 1000).toLong())
            }
        }
    }
}