package com.example.keepalivedemo.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * 监听通知栏的通知让进程成为处理通知的永生进程，需要授予权限，但在国内的某些手机上，进程被杀死了，就无法拉活
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class NotificationReceiveService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationService"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "onListenerConnected()")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "onNotificationPosted()")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "onNotificationRemoved()")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "onListenerDisconnected()")
    }
}
