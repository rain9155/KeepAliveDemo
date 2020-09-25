package com.example.keepalivedemo.daemon.nativ;

import android.content.Context;

import com.example.keepalivedemo.daemon.strategy.IDaemonStrategy;

/**
 * native code to watch each other when api
 */
public class NativeDaemonAPI{

    static {
        try {
            SoLoaderCompat.loadLibrary("daemon");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * used for native
     */
    protected Context mContext;

    public NativeDaemonAPI(Context context) {
        this.mContext = context;
    }

    /**
     * native call back
     */
    protected void onDaemonDead() {
        IDaemonStrategy.Fetcher.fetchStrategy().onDaemonDead();
    }

    @SuppressWarnings("JavaJniMissingFunction")
    public native void doDaemon(String indicatorSelfPath, String indicatorDaemonPath,
                                String observerSelfPath, String observerDaemonPath);
}
