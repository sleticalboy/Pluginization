package com.sleticalboy.pluginization

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_another.*

/**
 * Created on 19-7-12.
 * @author leebin
 */
class AnotherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_another)
        toast.setOnClickListener {
            Toast.makeText(this, "make a toast", Toast.LENGTH_SHORT).show()
        }
    }
}