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
    fun delete(key: String)
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
