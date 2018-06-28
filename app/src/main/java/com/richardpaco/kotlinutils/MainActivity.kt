package com.richardpaco.kotlinutils

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.richardpaco.kotlinutils.test.JumpEvent
import com.richardpaco.kotlinutils.test.ModifyEvent
import com.richardpaco.kotlinutils.test.UserInfo
import com.zskpaco.kotlinutils.*

/**
 * 基本操作：异步，uiThread，等待，toast，事件处理
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logi("start-register-${System.currentTimeMillis()}")

        subscriptions {
            subscribe(UserInfo::class) schedule Schedulers.ui observe {
                logi("UserInfo event ${Thread.currentThread()}")
            }

            subscribe(ModifyEvent::class) schedule Schedulers.async sticky {
                logi("ModifyEvent event ${Thread.currentThread()}")
            }

            subscribe(JumpEvent::class) observe {
                logi("JumpEvent event ${Thread.currentThread()}")
            }
        }

        UserInfo().post()

        ModifyEvent().post()

        JumpEvent().doAsync {
            post()
        }

    }

    override fun onDestroy() {
        super.onDestroy()


        logi("onDestroy")

    }
}
