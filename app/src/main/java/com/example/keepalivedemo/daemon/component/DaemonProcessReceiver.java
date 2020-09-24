package com.example.keepalivedemo.daemon.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.keepalivedemo.daemon.DaemonManager;

public class DaemonProcessReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DaemonManager.inst(context).initDaemon();
    }
}
