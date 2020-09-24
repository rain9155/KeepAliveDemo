package com.example.keepalivedemo.sswo

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import java.lang.ref.WeakReference

class ScreenActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ScreenActivity"

        private var mRef: WeakReference<Activity>? = null

        fun startActivity(context: Context){
            context.startActivity(Intent(context, ScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }

        fun finishActivity(){
            mRef?.get()?.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        mRef = WeakReference(this)

        window.attributes = window.attributes.apply {
            width = 1
            height = 1
            x = 0
            y = 0
        }
        window.setGravity(Gravity.START and Gravity.TOP)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(!isFinishing){
            finish()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }
}