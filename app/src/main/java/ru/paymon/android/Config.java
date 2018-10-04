package ru.paymon.android;

public class Config {
    public static final boolean DEBUG = false;
    public static final boolean PRODUCTION_VERSION = true;
    public static final boolean TEST_BITCOIN = false;

    public static String HOST;
    public static String HOST_ALT;

    public static final String TAG = "paymon-dbg";
    public static final String VERSION_STRING = "0.01";

//        public static short PORT = 7966;
    public static short PORT = 7968;

    static {
//        if (DEBUG) {
//            HOST = "192.168.88.168";
//            HOST_ALT = "192.168.88.168";
//            PORT = 7968;
//        } else {
        HOST = "91.226.80.26";
        HOST_ALT = "91.226.80.26";
//        }
    }

    public static final int VERSION = 0x1_08b;

    public static final int GAS_PRICE_DEFAULT = 40;
    public static final int GAS_LIMIT_DEFAULT = 21000;
    public static final int GAS_PRICE_MIN = 1;
    public static final int GAS_LIMIT_MIN = 21000;
    public static final int GAS_LIMIT_MAX = 300000;

    public static final int minAvatarSize = 256;
    public static final int maxAvatarSize = 512;
}
