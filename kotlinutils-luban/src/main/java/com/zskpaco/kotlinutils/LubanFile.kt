package com.zskpaco.kotlinutils

import java.io.File


fun <T : File> Collection<File>.compress(exception: ((Throwable) -> Unit)? = crashLogger, task: Array<File>.() -> Unit) {

}

fun <T : File> T.compress(exception: ((Throwable) -> Unit)? = crashLogger, task: Array<File>.() -> Unit) {

}

fun <T : File> Array<T>.compress(exception: ((Throwable) -> Unit)? = crashLogger, task: Array<File>.() -> Unit) {

}

