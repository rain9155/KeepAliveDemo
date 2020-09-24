package com.example.keepalivedemo.utils

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import com.example.keepalivedemo.service.NotificationReceiveService
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.Exception

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

    fun openSettings(context: Context){
        if(RomUtil.isHuawei()){
            openHuaweiSettings(context)
        }else if(RomUtil.isXiaomi()){
            openXiaomiSettings(context)
        }else if(RomUtil.isOppo()){
            openOppoSettings(context)
        }else if(RomUtil.isVivo()){
            openVivoSettings(context)
        }else if(RomUtil.isMeizu()){
            openMeizuSettings(context)
        }else if(RomUtil.isSamsung()){
            openSamsungSettings(context)
        }else if(RomUtil.isSmartisan()){
            openSmartisanSettings(context)
        }
    }

    fun openMainActivity(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName);
        context.startActivity(intent);
    }

    fun openActivity(context: Context, packageName: String, className: String) {
        val intent = Intent().apply {
            component = ComponentName(packageName, className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent);
    }

    fun openHuaweiSettings(context: Context){
        try {
            openActivity(context,
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        }catch (e1: Exception){
            try {
                openActivity(context,
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.bootstart.BootStartActivity")
            }catch (e2: Exception){
                e2.printStackTrace()
            }
        }
    }

    fun openXiaomiSettings(context: Context){
        try {
            openActivity(context,
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun openOppoSettings(context: Context) {
        try {
            openMainActivity(context, "com.coloros.phonemanager")
        } catch (e1: Exception) {
            try {
                openMainActivity(context, "com.oppo.safe")
            } catch (e2: Exception) {
                try {
                    openMainActivity(context, "com.coloros.oppoguardelf")
                } catch (e3: Exception) {
                    try {
                        openMainActivity(context, "com.coloros.safecenter")
                    }catch (e4: Exception){
                        e4.printStackTrace()
                    }
                }
            }
        }
    }

    fun openVivoSettings(context: Context){
        try {
            openMainActivity(context, "com.iqoo.secure")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun openMeizuSettings(context: Context){
        try {
            openMainActivity(context, "com.meizu.safe")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun openSamsungSettings(context: Context){
        try {
            openMainActivity(context, "com.samsung.android.sm_cn")
        }catch (e1: Exception){
            try {
                openMainActivity(context, "com.samsung.android.sm")
            }catch (e2: Exception){
                e2.printStackTrace()
            }
        }
    }

    fun openSmartisanSettings(context: Context){
        try {
            openMainActivity(context, "com.smartisanos.security")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private var sCurProcessName: String? = null
    @JvmStatic
    fun getCurProcessName(context: Context): String? {
        var procName = sCurProcessName
        if (!TextUtils.isEmpty(procName)) {
            return procName
        }
        try{
            val pid = Process.myPid()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.runningAppProcesses.forEach {appProcess ->
                if (appProcess.pid == pid) {
                    sCurProcessName = appProcess.processName
                    return sCurProcessName
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
        sCurProcessName = getCurProcessNameFromProc()
        return sCurProcessName
    }

    @JvmStatic
    fun getCurProcessNameFromProc(): String? {
        var cmdlineReader: BufferedReader? = null
        val processName = StringBuilder()
        try {
            cmdlineReader = BufferedReader(InputStreamReader(FileInputStream("/proc/" + Process.myPid() + "/cmdline"), "iso-8859-1"))
            var c = cmdlineReader.read()
            while (c > 0) {
                processName.append(c.toChar())
                c = cmdlineReader.read()
            }
            return processName.toString()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            try {
                cmdlineReader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return processName.toString()
    }

}