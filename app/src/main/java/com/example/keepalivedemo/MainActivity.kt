package com.example.keepalivedemo

import android.content.*
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.keepalivedemo.daemon.DaemonManager
import com.example.keepalivedemo.sswo.ScreenManager
import com.example.keepalivedemo.service.ForegroundService
import com.example.keepalivedemo.service.JobSchedulerService
import com.example.keepalivedemo.supervisor.SupervisorForegroundService
import com.example.keepalivedemo.supervisor.SupervisorManager
import com.example.keepalivedemo.utils.Util
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerComponentCallbacks(object : ComponentCallbacks2{

            override fun onLowMemory() {
                Log.d(TAG, "onLowMemory()")
            }

            override fun onTrimMemory(level: Int) {
                Log.d(TAG, "onTrimMemory(), level = $level")
            }

            override fun onConfigurationChanged(newConfig: Configuration) {
                Log.d(TAG, "onConfigurationChanged()")
            }
        })

        btn_start_foreground.setOnClickListener {
            val intent = Intent(this, ForegroundService::class.java)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                startForegroundService(intent)
            }else{
                startService(intent)
            }
        }

        btn_start_activity.setOnClickListener {
            ScreenManager.getInstance(this).register()
        }

        btn_start_scheduler.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                JobSchedulerService.schedulerJob(this)
            }
        }

        btn_start_daemon.setOnClickListener {
            DaemonManager.inst(this).initDaemon()
        }

        btn_start_native.setOnClickListener {
            SupervisorManager.inst(this).startKeepAlive()
        }

        btn_request_notification.setOnClickListener {
            if(!Util.isNotificationListenerEnabled(this)){
                Util.openNotificationListenSettings(this)
            }else{
                Util.reBindNotificationListenerService(this)
            }
        }

        btn_request_battery_whitelist.setOnClickListener {
            if(!Util.isIgnoringBatteryOptimizations(this)){
                Util.openIgnoreBatteryOptimizationsSettings(this)
            }
        }

        btn_request_auto_whitelist.setOnClickListener {
            Util.openSettings(this)
        }
    }

}