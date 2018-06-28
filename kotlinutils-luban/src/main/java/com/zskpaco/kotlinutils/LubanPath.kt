package com.zskpaco.kotlinutils

import java.io.File


fun <T : String> Collection<T>.compress(exception: ((Throwable) -> Unit)? = crashLogger, task: Array<File>.() -> Unit) {

}

fun <T : String> T.compress(exception: ((Throwable) -> Unit)? = crashLogger, task: Array<File>.() -> Unit) {

}

fun <T : String> Array<T>.compress(exception: ((Throwable) -> Unit)? = crashLogger, task: Array<File>.() -> Unit) {

}

