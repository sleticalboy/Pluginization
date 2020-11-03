package com.binlee.pluginization

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected() name = $name, service = $service")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected() name = $name")
        }
    }
    private val mReceiver = AnotherReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity.setOnClickListener {
            startActivity(Intent(this, AnotherActivity::class.java))
        }

        // start & stop service
        startService.setOnClickListener {
            startService(Intent(this, AnotherService::class.java))
        }
        stopService.setOnClickListener {
            stopService(Intent(this, AnotherService::class.java))
        }

        // bind & unbind service
        bindService.setOnClickListener {
            bindService(Intent(this, AnotherService::class.java),
                mConnection, BIND_AUTO_CREATE)
        }
        unbindService.setOnClickListener {
            unbindService(mConnection)
        }

        // register & unregister receiver
        register.setOnClickListener {
            registerReceiver(mReceiver, IntentFilter(Intent.ACTION_ANSWER))
        }
        unregister.setOnClickListener {
            unregisterReceiver(mReceiver)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
