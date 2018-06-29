package com.zskpaco.kotlinutils

import java.io.Serializable

/**
 * Author: Richard paco
 * Date: 2018/6/29
 * Desc:
 */

/**
 * 事件
 */
interface Subscriptions

interface FixSubscribe< S>

/**
 * 节流器
 */
interface Throttle<T>

/**
 * 计时器
 */
interface Internal<T>

/**
 * SharedPreferences
 */
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
