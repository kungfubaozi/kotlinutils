package com.richardpaco.kotlinutils

import android.app.Application
import com.zskpaco.kotlinutils.enableLogger
import com.zskpaco.kotlinutils.enableNotifyService
import com.zskpaco.kotlinutils.enableSharedPreferences

/**
 * Author: Richard paco
 * Date: 2018/6/26
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()



        enableNotifyService()

        enableLogger()

        enableSharedPreferences("")
    }
}