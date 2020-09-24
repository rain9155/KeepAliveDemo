package com.example.keepalivedemo.daemon;

import android.content.Context;
import android.util.Log;

import com.example.keepalivedemo.daemon.component.DaemonProcessReceiver;
import com.example.keepalivedemo.daemon.component.DaemonProcessService;
import com.example.keepalivedemo.daemon.component.MainProcessReceiver;
import com.example.keepalivedemo.daemon.component.MainProcessService;
import com.example.keepalivedemo.daemon.nativ.SoLoaderCompat;
import com.example.keepalivedemo.daemon.strategy.IDaemonStrategy;
import com.example.keepalivedemo.utils.Util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 原理分析 详见: http://blog.csdn.net/marswin89/article/details/50916631
 * <p>
 * 大致流程:
 * 1、4个文件，a进程文件a1，a2，b进程b1，b2
 * 2、a进程加锁文件a1，b进程同理
 * 3、a进程创建a2文件，然后轮询查看b2文件是否存在（这里可以轮询，因为时间很短），不存在代表b进程还没创建，b进程同理
 * 4、a进程轮询到b2文件存在了，代表b进程已经创建并可能在对b1文件加锁，此时删除文件b2，代表a进程已经加锁完毕，允许b进程读取a进程的锁，b进程同理
 * 5、a进程监听文件a2，如果a2被删除，代表b进程进行到了步骤4已经对b1加锁完成，可以开始读取b1文件的锁（不能直接监听a2文件删除，也就是不能跳过34步，这也是最难想的一部分，如果那样可能此时b进程还没创建，和b进程创建完成并加锁完成的状态是一样的，就会让进程a误以为进程b加锁完成），b进程同理
 */
public class DaemonManager {

    private static final String TAG = "DaemonManager";

    private volatile static DaemonManager sInstance;

    public static DaemonManager inst(Context context) {
        if (sInstance == null) {
            synchronized (DaemonManager.class) {
                if (sInstance == null) {
                    sInstance = new DaemonManager(context);
                }
            }
        }
        return sInstance;
    }

    private Context mContext;
    private AtomicBoolean mIsInited = new AtomicBoolean(false);
    private DaemonConfigurations mConfigurations;
    private DaemonConfigurations.DaemonListener mDaemonListener;


    public void setDaemonConfigurations(DaemonConfigurations daemonConfigurations) {
        mConfigurations = daemonConfigurations;
    }

    public void setSoLoader(SoLoaderCompat.SoLoader soLoader) {
        SoLoaderCompat.setSoLoader(soLoader);
    }

    private DaemonManager(Context context) {
        try {
            mContext = context.getApplicationContext();
            mConfigurations = buildDaemonConfigurations();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 初始化双进程保活
     */
    public void initDaemon() {
        try {
            if (mIsInited.getAndSet(true)) {
                return;
            }

            String processName = Util.getCurProcessName(mContext);

            Log.d(TAG, "processName = " + processName);
            Log.d(TAG, "mConfigurations.PERSISTENT_CONFIG.PROCESS_NAME = " + mConfigurations.PERSISTENT_CONFIG.PROCESS_NAME);
            Log.d(TAG, "mConfigurations.DAEMON_ASSISTANT_CONFIG.PROCESS_NAME = " + mConfigurations.DAEMON_ASSISTANT_CONFIG.PROCESS_NAME);

            if (processName.endsWith(mConfigurations.PERSISTENT_CONFIG.PROCESS_NAME)) {
                IDaemonStrategy.Fetcher.fetchStrategy().onPersistentCreate(mContext, mConfigurations);
            } else if (processName.endsWith(mConfigurations.DAEMON_ASSISTANT_CONFIG.PROCESS_NAME)) {
                IDaemonStrategy.Fetcher.fetchStrategy().onDaemonAssistantCreate(mContext, mConfigurations);
            }
        } catch (Throwable t) {
            mIsInited.set(false);
            t.printStackTrace();
        }
    }

    private DaemonConfigurations buildDaemonConfigurations() {

        DaemonConfigurations.DaemonConfiguration persistentConfig = new DaemonConfigurations.DaemonConfiguration(
                mContext.getPackageName(),
                MainProcessService.class.getCanonicalName(),
                MainProcessReceiver.class.getCanonicalName());

        DaemonConfigurations.DaemonConfiguration daemonAssistantConfig = new DaemonConfigurations.DaemonConfiguration(
                mContext.getPackageName() + ":daemon",
                DaemonProcessService.class.getCanonicalName(),
                DaemonProcessReceiver.class.getCanonicalName());

        DaemonConfigurations.DaemonListener listener = new DefaultDaemonListener();

        return new DaemonConfigurations(persistentConfig, daemonAssistantConfig, listener);
    }

    static class DefaultDaemonListener implements DaemonConfigurations.DaemonListener {

        @Override
        public void onPersistentStart(Context context) {
            Log.d(TAG, "onPersistentStart");
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
            Log.d(TAG, "onDaemonAssistantStart");
        }

        @Override
        public void onWatchDaemonDead() {
            Log.d(TAG, "onWatchDaemonDead");

        }

    }

}
