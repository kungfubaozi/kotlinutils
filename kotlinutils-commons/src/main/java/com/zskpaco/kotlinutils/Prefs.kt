package com.zskpaco.kotlinutils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import java.io.*

fun Application.enableSharedPreferences(name: String) {
    SharedPreferencesOperator.instance = this.getSharedPreferences(name, Context.MODE_PRIVATE)
}

@SuppressLint("CommitPrefEdits")
infix fun String.prefs(key: String): String {
    editor().putString(key, this).commit()
    return this
}

@SuppressLint("CommitPrefEdits")
infix fun Boolean.prefs(key: String): Boolean {
    editor().putBoolean(key, this).commit()
    return this
}

@SuppressLint("CommitPrefEdits")
infix fun Long.prefs(key: String): Long {
    editor().putLong(key, this).commit()
    return this
}

@SuppressLint("CommitPrefEdits")
infix fun Int.prefs(key: String): Int {
    editor().putInt(key, this).commit()
    return this
}

@SuppressLint("CommitPrefEdits")
infix fun <T : Serializable> T.prefs(key: String): T {
    editor()
    SharedPreferencesOperator().putSerializable(key, this)
    return this
}

fun sharedPreferences(operator: PreferencesOperate.() -> Unit): Preferences {
    editor()
    val sp = SharedPreferencesOperator()
    sp.operator()
    return sp
}

@SuppressLint("CommitPrefEdits")
private fun editor(): SharedPreferences.Editor {
    SharedPreferencesOperator.editor = SharedPreferencesOperator.instance.edit()
    return SharedPreferencesOperator.editor
}

interface Preferences {
    fun commit()
}

interface PreferencesOperate {
    fun putBoolean(key: String, value: Boolean)
    fun putString(key: String, value: String)
    fun putInt(key: String, value: Int)
    fun putFloat(key: String, value: Float)
    fun putLong(key: String, value: Long)
    fun putSerializable(key: String, value: Serializable)
    fun getBoolean(key: String): Boolean
    fun getString(key: String): String
    fun getInt(key: String): Int
    fun getFloat(key: String): Float
    fun getLong(key: String): Long
    fun <T : Serializable> getSerializable(key: String): T?
}

internal class SharedPreferencesOperator : PreferencesOperate, Preferences {

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
