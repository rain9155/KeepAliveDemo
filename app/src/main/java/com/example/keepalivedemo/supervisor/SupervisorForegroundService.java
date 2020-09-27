package com.example.keepalivedemo.supervisor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.keepalivedemo.service.ForegroundService;

import org.jetbrains.annotations.Nullable;

public class SupervisorForegroundService extends ForegroundService {

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        SupervisorManager.inst(this).startKeepAlive();
        return super.onStartCommand(intent, flags, startId);
    }
}
