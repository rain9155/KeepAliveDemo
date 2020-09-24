package com.example.keepalivedemo.daemon.strategy;

import android.content.Context;
import android.os.Build;

import com.example.keepalivedemo.daemon.DaemonConfigurations;


/**
 * define strategy method
 *
 * @author Mars
 */
public interface IDaemonStrategy {
    /**
     * when Persistent process create
     *
     * @param context
     * @param configs
     */
    void onPersistentCreate(Context context, DaemonConfigurations configs);

    /**
     * when DaemonAssistant process create
     *
     * @param context
     * @param configs
     */
    void onDaemonAssistantCreate(Context context, DaemonConfigurations configs);

    /**
     * when watches the process dead which it watched
     */
    void onDaemonDead();


    /**
     * all about strategy on different device here
     *
     * @author Mars
     */
    class Fetcher {

        private static IDaemonStrategy mDaemonStrategy;

        /**
         * fetch the strategy for this device
         *
         * @return the daemon strategy for this device
         */
        public static IDaemonStrategy fetchStrategy() {
            if (mDaemonStrategy != null) {
                return mDaemonStrategy;
            }
            int sdk = Build.VERSION.SDK_INT;
            if (sdk >= Build.VERSION_CODES.O){
                // Android 8 启动模式变了,这里先使用系统默认的模式
                mDaemonStrategy = new DaemonStrategy26();
            } else if (sdk >= Build.VERSION_CODES.M) {
                mDaemonStrategy = new DaemonStrategy23();
            } else {
                mDaemonStrategy = new DaemonStrategy21();
            }
            return mDaemonStrategy;
        }
    }
}
