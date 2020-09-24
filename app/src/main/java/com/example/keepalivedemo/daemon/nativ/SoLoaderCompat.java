package com.example.keepalivedemo.daemon.nativ;

public class SoLoaderCompat {

    private static SoLoader sSoLoader;

    public interface SoLoader {
        /**
         * load so lib
         */
        boolean loadLibrary(String libName);
    }

    static class DefaultSoLoader implements SoLoader {

        @Override
        public boolean loadLibrary(String libName) {
            System.loadLibrary(libName);
            return true;
        }
    }

    public static void setSoLoader(SoLoader soLoader) {
        sSoLoader = soLoader;
    }

    public static void loadLibrary(String libName) {
        if (sSoLoader != null && sSoLoader.loadLibrary(libName)) {
            return;
        }
        new DefaultSoLoader().loadLibrary(libName);
    }
}
