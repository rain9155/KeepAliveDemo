package com.example.keepalivedemo.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import com.example.keepalivedemo.service.NotificationReceiveService

/**
 *
 * @author chenjianyu
 * @date 2020/9/10
 */
object Util{

    fun isNotificationListenerEnabled(context: Context): Boolean{
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }

    fun openNotificationListenSettings(context: Context){
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        } else {
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        }
        context.startActivity(intent)
    }

    fun reBindNotificationListenerService(context: Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationListenerService.requestRebind(ComponentName(context, NotificationReceiveService::class.java))
        }else{
            val pm = context.packageManager
            pm.setComponentEnabledSetting(ComponentName(context, NotificationReceiveService::class.java), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(ComponentName(context, NotificationReceiveService::class.java), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean{
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    fun openIgnoreBatteryOptimizationsSettings(context: Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            })
        }
    }
}