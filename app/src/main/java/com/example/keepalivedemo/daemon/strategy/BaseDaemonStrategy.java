package com.example.keepalivedemo.daemon.strategy;

import android.content.Context;

import com.example.keepalivedemo.daemon.DaemonConfigurations;
import com.example.keepalivedemo.daemon.nativ.NativeDaemonAPI;

import java.io.File;

/**
 * Created by wanonce on 2017/3/21.
 */

public class BaseDaemonStrategy implements IDaemonStrategy {

    protected static final String ACTION_START_BY_DAEMON = "com.ss.android.push.daemon.START";
    protected final static String INDICATOR_DIR_NAME = "indicators";
    protected final static String INDICATOR_PERSISTENT_FILENAME = "indicator_p";
    protected final static String INDICATOR_DAEMON_ASSISTANT_FILENAME = "indicator_d";
    protected final static String OBSERVER_PERSISTENT_FILENAME = "observer_p";
    protected final static String OBSERVER_DAEMON_ASSISTANT_FILENAME = "observer_d";

    protected Context mContext;
    protected DaemonConfigurations mConfigs;
    protected boolean isPersistentCreate;

    @Override
    public void onPersistentCreate(Context context, DaemonConfigurations configs) {
        mContext = context.getApplicationContext();
        mConfigs = configs;
        isPersistentCreate = true;
    }

    @Override
    public void onDaemonAssistantCreate(Context context, DaemonConfigurations configs) {
        mContext = context.getApplicationContext();
        mConfigs = configs;
        isPersistentCreate = false;
    }

    @Override
    public void onDaemonDead() {
        if(mConfigs != null){
            mConfigs.LISTENER.onWatchDaemonDead();
        }
    }

    protected boolean doDaemon(){
        String indicatorSelfPath;
        String indicatorDaemonPath;
        String observerSelfPath;
        String observerDaemonPath;
        if(isPersistentCreate){
            indicatorSelfPath = INDICATOR_PERSISTENT_FILENAME;
            indicatorDaemonPath = INDICATOR_DAEMON_ASSISTANT_FILENAME;
            observerSelfPath = OBSERVER_PERSISTENT_FILENAME;
            observerDaemonPath = OBSERVER_DAEMON_ASSISTANT_FILENAME;
        }else {
            indicatorSelfPath = INDICATOR_DAEMON_ASSISTANT_FILENAME;
            indicatorDaemonPath = INDICATOR_PERSISTENT_FILENAME;
            observerSelfPath = OBSERVER_DAEMON_ASSISTANT_FILENAME;
            observerDaemonPath = OBSERVER_PERSISTENT_FILENAME;
        }

        try {

            File indicatorDir = mContext.getDir( INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
            new NativeDaemonAPI(mContext).doDaemon(
                    new File(indicatorDir, indicatorSelfPath).getAbsolutePath(),
                    new File(indicatorDir, indicatorDaemonPath).getAbsolutePath(),
                    new File(indicatorDir, observerSelfPath).getAbsolutePath(),
                    new File(indicatorDir, observerDaemonPath).getAbsolutePath());

            if(mConfigs != null){
                if(isPersistentCreate){
                    mConfigs.LISTENER.onPersistentStart(mContext);
                }else {
                    mConfigs.LISTENER.onDaemonAssistantStart(mContext);
                }
            }
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }
}
