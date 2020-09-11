package com.example.keepalivedemo.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * JobService定时拉活进程，在8.0以后的系统上进程杀死了，就无法拉活
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerService : JobService() {

    companion object{
        private const val TAG = "JobSchedulerService"
        private const val PERIOD = 1000L * 60 * 15

        fun schedulerJob(context: Context): Int {
            val jobBuild = JobInfo.Builder(1, ComponentName(context, JobSchedulerService::class.java))
                .setPeriodic(PERIOD)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            return jobScheduler.schedule(jobBuild.build())
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
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

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStartJob()")
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob()")
        return false
    }
}
