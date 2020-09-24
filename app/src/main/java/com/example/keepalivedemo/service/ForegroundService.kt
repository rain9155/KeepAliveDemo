package com.example.keepalivedemo.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * 前台服务提高进程优先级
 */
class ForegroundService : Service() {

    companion object{
        private const val TAG = "ForegroundService"
        private const val NOTIFICATION_ID = 1

        private fun createNotification(context: Context): Notification? {
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("1", "channel", NotificationManager.IMPORTANCE_HIGH)
                val notificationManager = NotificationManagerCompat.from(context);
                notificationManager.createNotificationChannel(channel)
                NotificationCompat.Builder(context, channel.id).build()
            } else {
                NotificationCompat.Builder(context).build()
            }
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        val notification =
            createNotification(
                this
            )
        startForeground(NOTIFICATION_ID, notification)
        //启动前台服务后，会在通知栏显示"应用正在运行"的通知，在 18<=api<=24 可以启动一个Service绑定
        //同一个通知，然后在启动的Service中取消掉这个通知和Service达到取消这个"应用正在运行"通知的目的
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N){
            startService(Intent(this, RemoveNotificationService::class.java))
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind()")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind()")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
    }

    class RemoveNotificationService : IntentService("RemoveNotificationService") {

        override fun onCreate() {
            super.onCreate()
            startForeground(NOTIFICATION_ID, createNotification(this))
        }

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onHandleIntent(intent: Intent?) {
            TODO("Not yet implemented")
        }

        override fun onDestroy() {
            super.onDestroy()
            stopForeground(true)
            NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        }
    }

}
