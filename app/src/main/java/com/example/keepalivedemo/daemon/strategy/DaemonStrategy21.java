package com.example.keepalivedemo.daemon.strategy;

import android.annotation.SuppressLint;
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
 * the strategy in android API 21.
 */
public class DaemonStrategy21 extends BaseDaemonStrategy {

    private IBinder mRemote;
    private Parcel mServiceData;

    /**
     * 当 push 进程启动时
     * 1. 初始化 AMS binder
     * 2. 先做好 Parcel
     * 3. 运行 native 保活方法
     */
    @Override
    public void onPersistentCreate(final Context context, DaemonConfigurations configs) {
        super.onPersistentCreate(context, configs);
        initAmsBinder();
        if(null == mRemote){
            return;
        }
        initServiceParcel(context, configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
        startServiceByAmsBinder();
        doDaemon();
    }

    /**
     * 当 pushservice 进程启动
     */
    @Override
    public void onDaemonAssistantCreate(final Context context, DaemonConfigurations configs) {
        super.onDaemonAssistantCreate(context, configs);
        initAmsBinder();
        if(null == mRemote){
            return;
        }
        initServiceParcel(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);
        startServiceByAmsBinder();
        doDaemon();
    }


    @Override
    public void onDaemonDead() {
        super.onDaemonDead();
        startServiceByAmsBinder();
        doDaemon();
    }

    /**
     * 先获取到 AMS 的 binder
     */
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
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    @SuppressLint("Recycle")
// when process dead, we should save time to restart and kill self, don`t take a waste of time to recycle
    /**
     * 在调用前先做好 Parcel 工作
     * 每个进程都会调用这个方法, 且传递的参数是不同的
     */
    private synchronized void initServiceParcel(Context context, String serviceName) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName(context.getPackageName(), serviceName);
        intent.setAction(ACTION_START_BY_DAEMON);
        intent.setComponent(component);
        //write pacel
        mServiceData = Parcel.obtain();
        mServiceData.writeInterfaceToken("android.app.IActivityManager");
        mServiceData.writeStrongBinder(null);
        intent.writeToParcel(mServiceData, 0);
        mServiceData.writeString(null);
        mServiceData.writeInt(0);
    }


    /**
     * 直接用 之前获取到的 AMS 和Parcel 直接发送 binder 调用
     * @return
     */
    private boolean startServiceByAmsBinder() {
        try {
            if (mRemote == null || mServiceData == null) {
                Log.e("Daemon", "REMOTE IS NULL or PARCEL IS NULL !!!");
                return false;
            }
            mRemote.transact(34, mServiceData, null, 0);//START_SERVICE_TRANSACTION = 34
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
