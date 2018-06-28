package com.richardpaco.kotlinutils

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.richardpaco.kotlinutils.test.JumpEvent
import com.richardpaco.kotlinutils.test.ModifyEvent
import com.zskpaco.kotlinutils.*
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Author: Richard paco
 * Date: 2018/6/21
 */
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            ModifyEvent().postSticky()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        subscriptions {
            subscribe(JumpEvent::class) sticky {
                logi("receiver jump event")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribe()
    }
}
