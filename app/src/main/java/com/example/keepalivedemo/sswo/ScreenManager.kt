package com.example.keepalivedemo.sswo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PowerManager
import android.view.Display

/**
 * 监听锁/开屏广播，启动/关闭1像素Activity提高进程优先级, 但在国内的某些手机或当api >= 29时，从后台启动Activity可能会不成功
 * @author chenjianyu
 * @date 2020/9/10
 */
class ScreenManager private constructor(private val context: Context){

    private var receiver: BroadcastReceiver =
        ScreenReceiver()

    companion object{
        private var INSTANCE : ScreenManager? = null

        fun getInstance(context: Context): ScreenManager {
            return INSTANCE
                ?: synchronized(this){
                INSTANCE
                    ?: ScreenManager(context.applicationContext)
                        .apply {
                    INSTANCE = this
                }
            }
        }
    }


    fun register(){
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(receiver, intentFilter)
    }

    fun unregister(){
        context.unregisterReceiver(receiver)
    }

    fun startActivity(){
        ScreenActivity.startActivity(context)
    }

    fun finishActivity(){
        ScreenActivity.finishActivity()
    }

    fun isScreenOff(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val displays = displayManager.displays
            if (displays != null && displays.isNotEmpty()) {
                if(Display.STATE_OFF == displays[0].state) {
                    return true
                }
            }
        } else {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return !powerManager.isScreenOn
        }
        return false;
    }
}