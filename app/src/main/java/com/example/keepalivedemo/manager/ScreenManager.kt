package com.example.keepalivedemo.manager

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.keepalivedemo.ScreenActivity
import com.example.keepalivedemo.receiver.ScreenReceiver
import java.lang.ref.WeakReference

/**
 * 监听锁/开屏广播，启动/关闭1像素Activity提高进程优先级, 但在国内的某些手机或当api >= 29时，从后台启动Activity可能会不成功
 * @author chenjianyu
 * @date 2020/9/10
 */
class ScreenManager private constructor(private val context: Context){

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

    private var activity: WeakReference<Activity>? = null
    private var receiver: BroadcastReceiver? = null

    fun register(){
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        if(receiver == null){
            receiver = ScreenReceiver()
        }else{
            context.unregisterReceiver(receiver)
        }
        context.registerReceiver(receiver, intentFilter)
    }

    fun setActivity(activity: Activity){
        this.activity = WeakReference(activity)
    }

    fun startActivity(){
        context.startActivity(Intent(context, ScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun finishActivity(){
        activity?.get()?.finish()
    }

}