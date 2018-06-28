package com.zskpaco.kotlinutils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kotlin.reflect.KClass

private object Logger {
    var enabled: Boolean = false
}

/**
 * Logger
 */
fun <T : Application> T.enableLogger() {
    Logger.enabled = true
}

fun <T : Any> T.logi(message: String) {
    if (Logger.enabled) Log.i("@logger", "@${this.javaClass.simpleName}->$message")
}

fun <T : Any> T.logi() {
    if (Logger.enabled) Log.i("@logger", "@${this.javaClass.simpleName}->$this")
}

/**
 * Toast
 */
fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.toastLong(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Activity.startActivity(activity: Class<Activity>) {
    this.startActivity(Intent(this, activity))
}