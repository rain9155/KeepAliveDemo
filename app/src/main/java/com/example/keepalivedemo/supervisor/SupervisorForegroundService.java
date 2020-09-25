package com.example.keepalivedemo.supervisor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.keepalivedemo.service.ForegroundService;

public class SupervisorForegroundService extends ForegroundService {

    private static final String TAG = "NativeForegroundService";

    public static void startService(Context context){
        Intent intent = new Intent(context, SupervisorForegroundService.class);
        context.startService(intent);
    }

    public static void stopService(Context context){
        context.stopService(new Intent(context, SupervisorForegroundService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SupervisorManager.inst(this).startKeepAlive();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SupervisorManager.inst(this).stopKeepAlive();
    }
}
