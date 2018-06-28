package com.richardpaco.kotlinutils

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.richardpaco.kotlinutils.test.JumpEvent
import com.richardpaco.kotlinutils.test.ModifyEvent
import com.richardpaco.kotlinutils.test.UserInfo
import com.zskpaco.kotlinutils.*
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 基本操作：异步，uiThread，等待，toast，事件处理
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logi("start-register-${System.currentTimeMillis()}")

        subscriptions {
            subscribe(ModifyEvent::class) sticky {
                logi("receiver modify event")
            }

            subscribe(UserInfo::class) just {
                logi("username ${this.data.username}")

                logi("end-receiver-${System.currentTimeMillis()}")
            }

        }

        logi("end-register-${System.currentTimeMillis()}")

        UserInfo().apply {
            username = "username"
            password = "password"
        }.post()

        button.setOnClickListener {
            JumpEvent().postSticky()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribe()

        logi("onDestroy")

    }
}
