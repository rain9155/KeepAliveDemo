package com.example.keepalivedemo.daemon.component;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.keepalivedemo.daemon.DaemonManager;

public class DaemonProcessService extends Service {

    public DaemonProcessService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DaemonManager.inst(this).initDaemon();
        return super.onStartCommand(intent, flags, startId);
    }
}
