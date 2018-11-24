package ru.paymon.android;

import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;

public class Config {
    public static final boolean DEBUG = true;

    public static String HOST = "91.226.80.26";
    public static short PORT = DEBUG ? 7966 : 7968;

    public static final String TAG = "paymon-dbg";
    public static final String VERSION_STRING = "1.00";
    public static final int VERSION = 0x1_1;

    public static final int GAS_PRICE_DEFAULT = 40;
    public static final int GAS_LIMIT_DEFAULT = 21000;
    public static final int GAS_LIMIT_CONTRACT_DEFAULT = 200000;
    public static final int GAS_PRICE_MIN = 1;
    public static final int GAS_LIMIT_MIN = 21000;
    public static final int GAS_LIMIT_MAX = 300000;

    public static final int minAvatarSize = 256;
    public static final int maxAvatarSize = 512;

    public static final String MESSAGES_NOTIFICATION_CHANNEL_ID = "MESSAGES_NOTIFICATION_CHANNEL_ID";
    public static final String MESSAGES_NOTIFICATION_CHANNEL_NAME = "Messages";

    private static final HashSet<String> fiatCurrenciesSet = new HashSet<String>() {
        {
            add("USD");
            add("EUR");
            add(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        }
    };

    public static String[] fiatCurrencies;
    static {
        fiatCurrencies = Arrays.asList(fiatCurrenciesSet.toArray()).toArray(new String[fiatCurrenciesSet.toArray().length]);
    }
}
