package com.example.keepalivedemo.daemon.strategy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

import com.example.keepalivedemo.daemon.DaemonConfigurations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * the strategy in android API 23.
 */
public class DaemonStrategy23 extends BaseDaemonStrategy {

    private IBinder mRemote;
    private Parcel mBroadcastData;

    @Override
    public void onPersistentCreate(final Context context, DaemonConfigurations configs) {
        super.onPersistentCreate(context, configs);
        initAmsBinder();
        if(null == mRemote){
            return;
        }
        initBroadcastParcel(context, configs.DAEMON_ASSISTANT_CONFIG.RECEIVER_NAME);
        sendBroadcastByAmsBinder();
        doDaemon();
    }

    @Override
    public void onDaemonAssistantCreate(final Context context, DaemonConfigurations configs) {
        super.onDaemonAssistantCreate(context, configs);
        initAmsBinder();
        if(null == mRemote){
            return;
        }
        initBroadcastParcel(context, configs.PERSISTENT_CONFIG.RECEIVER_NAME);
        sendBroadcastByAmsBinder();
        doDaemon();
    }


    @Override
    public void onDaemonDead() {
        sendBroadcastByAmsBinder();
        doDaemon();
    }

    private void initAmsBinder() {
        Class<?> activityManagerNative;
        try {
            activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Object amn = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
            Field mRemoteField = amn.getClass().getDeclaredField("mRemote");
            mRemoteField.setAccessible(true);
            mRemote = (IBinder) mRemoteField.get(amn);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("Recycle")
    // when process dead, we should save time to restart and kill self, don`t take a waste of time to recycle
    private synchronized void initBroadcastParcel(Context context, String broadcastName) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(context.getPackageName(), broadcastName);
        intent.setComponent(componentName);
        intent.setAction(ACTION_START_BY_DAEMON);
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        mBroadcastData = Parcel.obtain();
        mBroadcastData.writeInterfaceToken("android.app.IActivityManager");
        mBroadcastData.writeStrongBinder(null);
        intent.writeToParcel(mBroadcastData, 0);
        mBroadcastData.writeString(intent.resolveTypeIfNeeded(context.getContentResolver()));
        mBroadcastData.writeStrongBinder(null);
        mBroadcastData.writeInt(Activity.RESULT_OK);
        mBroadcastData.writeString(null);
        mBroadcastData.writeBundle(null);
        mBroadcastData.writeString(null);
        mBroadcastData.writeInt(-1);
        mBroadcastData.writeInt(0);
        mBroadcastData.writeInt(0);
        mBroadcastData.writeInt(0);
    }


    private boolean sendBroadcastByAmsBinder() {
        try {
            if (mRemote == null || mBroadcastData == null) {
                Log.e("Daemon", "REMOTE IS NULL or PARCEL IS NULL !!!");
                return false;
            }
            mRemote.transact(14, mBroadcastData, null, 0);//BROADCAST_INTENT_TRANSACTION = 0x00000001 + 13
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
