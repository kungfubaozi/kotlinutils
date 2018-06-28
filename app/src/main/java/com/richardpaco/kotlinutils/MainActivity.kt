package com.richardpaco.kotlinutils

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.richardpaco.kotlinutils.test.UserInfo
import com.zskpaco.kotlinutils.*

/**
 * 基本操作：异步，uiThread，等待，toast，事件处理
 */
class MainActivity : AppCompatActivity() {

    val time = 9f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.button)

        subscriptions {
            subscribe(UserInfo::class) schedule Schedulers.ui observe {

            }
        }

        button throttle time clicks {
            toast("this is test throttle")
        }

        button.interval(10f) {
            next {
                logi("now internal at $this")
            }
            completed {
                logi("finished internal")
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()


        logi("onDestroy")

    }
}
