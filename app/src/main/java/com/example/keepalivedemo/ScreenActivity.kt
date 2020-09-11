package com.example.keepalivedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import com.example.keepalivedemo.manager.ScreenManager

class ScreenActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ScreenActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        ScreenManager.getInstance(this).setActivity(this)

        window.attributes = window.attributes.apply {
            width = 1
            height = 1
            x = 0
            y = 0
        }
        window.setGravity(Gravity.START and Gravity.TOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }
}