package com.example.keepalivedemo.daemon;

import android.content.Context;

/**
 * the configurations of Daemon SDK, contains two process configuration.
 */
public class DaemonConfigurations {

    public final DaemonConfiguration PERSISTENT_CONFIG;
    public final DaemonConfiguration DAEMON_ASSISTANT_CONFIG;
    public DaemonListener LISTENER;

    public DaemonConfigurations(DaemonConfiguration persistentConfig, DaemonConfiguration daemonAssistantConfig) {
        this(persistentConfig, daemonAssistantConfig, null);
    }

    public DaemonConfigurations(DaemonConfiguration persistentConfig, DaemonConfiguration daemonAssistantConfig, DaemonListener listener) {
        this.PERSISTENT_CONFIG = persistentConfig;
        this.DAEMON_ASSISTANT_CONFIG = daemonAssistantConfig;
        this.LISTENER = listener;
    }


    /**
     * the configuration of a daemon process, contains process name, service name and receiver name if Android 6.0
     */
    public static class DaemonConfiguration {

        public final String PROCESS_NAME;
        public final String SERVICE_NAME;
        public final String RECEIVER_NAME;

        public DaemonConfiguration(String processName, String serviceName, String receiverName) {
            this.PROCESS_NAME = processName;
            this.SERVICE_NAME = serviceName;
            this.RECEIVER_NAME = receiverName;
        }
    }

    /**
     * listener of daemon for external
     */
    public interface DaemonListener {

        /***
         * call when the main process created
         */
        void onPersistentStart(Context context);

        /**
         * call when the daemon process created
         */
        void onDaemonAssistantStart(Context context);

        /**
         * call when the daemon process dead
         */
        void onWatchDaemonDead();
    }
}
