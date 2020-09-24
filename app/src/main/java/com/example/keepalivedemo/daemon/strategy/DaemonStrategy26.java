package com.example.keepalivedemo.daemon.strategy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.keepalivedemo.daemon.DaemonConfigurations;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * the strategy in android API 26.
 */
public class DaemonStrategy26 extends BaseDaemonStrategy{

    @Override
    public void onPersistentCreate(Context context, DaemonConfigurations configs) {
        super.onPersistentCreate(context, configs);
        tryStartService();
        doDaemon();
    }

    @Override
    public void onDaemonAssistantCreate(Context context, DaemonConfigurations configs) {
        super.onDaemonAssistantCreate(context, configs);
        tryStartService();
        doDaemon();
    }

    @Override
    public void onDaemonDead() {
        super.onDaemonDead();
        if (isRepeat()) {
            return;
        }
        tryStartService();
        doDaemon();
    }

    private boolean tryStartService() {
        try {
            DaemonConfigurations.DaemonConfiguration configuration;
            if(isPersistentCreate){
                configuration = mConfigs.DAEMON_ASSISTANT_CONFIG;
            }else {
                configuration = mConfigs.PERSISTENT_CONFIG;
            }
            Intent intent = new Intent(mContext, Class.forName(configuration.SERVICE_NAME));
            intent.setAction(ACTION_START_BY_DAEMON);
            mContext.startService(intent);
            mContext.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            //8.0后台可能无法启动服务
            if(trySendBroadcastReceiver()){
                return true;
            }
        }
        return false;
    }

    private boolean trySendBroadcastReceiver(){
        try {
            DaemonConfigurations.DaemonConfiguration configuration;
            if(isPersistentCreate){
                configuration = mConfigs.DAEMON_ASSISTANT_CONFIG;
            }else {
                configuration = mConfigs.PERSISTENT_CONFIG;
            }
            Intent intent = new Intent(mContext, Class.forName(configuration.RECEIVER_NAME));
            intent.setAction(ACTION_START_BY_DAEMON);
            mContext.sendBroadcast(intent);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("DaemonStrategy", "bind service = " + name.getClassName());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("DaemonStrategy", "unbind service = " + name.getClassName());
            onDaemonDead();
        }
    };

    private volatile long mLastCallTime = 0;
    private static final long REPEAT_TIME_RATIO = 200;

    /**
     * 检查是否200 ms 以内重复调用
     */
    protected final boolean isRepeat() {
        long current = System.currentTimeMillis();
        if (current - mLastCallTime >= REPEAT_TIME_RATIO) {
            synchronized (this) {
                if (current - mLastCallTime >= REPEAT_TIME_RATIO) {
                    mLastCallTime = current;
                    return false;
                }
            }
        }
        return true;
    }
}
