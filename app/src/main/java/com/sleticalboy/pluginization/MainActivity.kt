package com.sleticalboy.pluginization

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity.setOnClickListener {
            startActivity(Intent(this, AnotherActivity::class.java))
        }

        startService.setOnClickListener {
            startService(Intent(this, AnotherService::class.java))
        }

        stopService.setOnClickListener {
            stopService(Intent(this, AnotherService::class.java))
        }
    }
}
