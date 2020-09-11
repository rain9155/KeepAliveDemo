package com.example.keepalivedemo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.keepalivedemo.manager.ScreenManager

class ScreenReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive(), action = " + intent.action)
        if(intent.action == Intent.ACTION_SCREEN_ON){
            ScreenManager.getInstance(context).finishActivity()
        }else if(intent.action == Intent.ACTION_SCREEN_OFF){
            ScreenManager.getInstance(context).startActivity()
        }
    }

}
