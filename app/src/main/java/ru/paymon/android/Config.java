package ru.paymon.android;

public class Config {
    public static final boolean DEBUG = false;
    public static final boolean PRODUCTION_VERSION = true;
    public static final boolean TEST_BITCOIN = false;

    public static String HOST;
    public static String HOST_ALT;

    public static final String TAG = "paymon-dbg";

    public static short PORT = 7966;
    static {
        if (DEBUG) {
            HOST = "192.168.88.168";
            HOST_ALT = "192.168.88.168";
            PORT = 7968;
        } else {
            HOST = "91.226.80.26";
            HOST_ALT = "91.226.80.26";
        }
    }

    public static final int VERSION = 0x1_08b;
    public static final String VERSION_STRING = "0.8 beta";

}
