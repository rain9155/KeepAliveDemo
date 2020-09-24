package com.example.keepalivedemo.sswo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive(), action = " + intent.action)
        if(intent.action == Intent.ACTION_SCREEN_ON
            || intent.action == Intent.ACTION_USER_PRESENT){
            ScreenManager.getInstance(context).finishActivity()
        }else if(intent.action == Intent.ACTION_SCREEN_OFF){
            ScreenManager.getInstance(context).startActivity()
        }
    }

}
