package com.example.keepalivedemo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * https://developer.android.com/guide/components/broadcast-exceptions?hl=zh-cn
 * 监听各种系统广播以拉活进程，从7.0、8.0开始系统开始逐渐禁止应用监听隐式广播
 */
class MessageReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MessageReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive(), action = " + intent.action)
    }

}
