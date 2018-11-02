package ru.paymon.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;

import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.SerializedStream;

public class User {

    //region UserPreferences
    private static final String USER_CONFIG = "USER_CONFIG";
    private static final String USER = "USER";
    public static RPC.PM_userSelf currentUser;
    //endregion

    //region ClientPreferences
    public static final String CLIENT_BASIC_DATE_FORMAT_LIST = "CLIENT_BASIC_DATE_FORMAT_LIST";
    public static final String CLIENT_CONF_HIDE_EMAIL_SWITCH = "CLIENT_CONF_HIDE_EMAIL_SWITCH";
    public static final String CLIENT_CONF_HIDE_PHONE_SWITCH = "CLIENT_CONF_HIDE_PHONE_SWITCH";
    public static final String CLIENT_CONF_HIDE_NAME_SWITCH = "CLIENT_CONF_HIDE_NAME_SWITCH";
    public static final String CLIENT_MESSAGES_NOTIFY_DONT_WORRY_SWITCH = "CLIENT_MESSAGES_NOTIFY_DONT_WORRY_SWITCH";
    public static final String CLIENT_MESSAGES_NOTIFY_VIBRATION_SWITCH = "CLIENT_MESSAGES_NOTIFY_VIBRATION_SWITCH";
    public static final String CLIENT_MESSAGES_NOTIFY_NOTIFICATIONS_ENABLED_SWITCH = "CLIENT_MESSAGES_NOTIFY_NOTIFICATIONS_ENABLED_SWITCH";
    public static final String CLIENT_MESSAGES_NOTIFY_TRANSACTIONS_SWITCH = "CLIENT_MESSAGES_NOTIFY_TRANSACTIONS_SWITCH";
    public static final String CLIENT_MESSAGES_NOTIFY_FRIENDS_INVITES_SWITCH = "CLIENT_MESSAGES_NOTIFY_FRIENDS_INVITES_SWITCH";
    public static final String CLIENT_MESSAGES_NOTIFY_GROUP_INVITES_SWITCH = "CLIENT_MESSAGES_NOTIFY_GROUP_INVITES_SWITCH";
    public static final String CLIENT_SECURITY_PASSWORD_ENABLED_CHECK = "CLIENT_SECURITY_PASSWORD_ENABLED_CHECK";
    public static final String CLIENT_SECURITY_PASSWORD_PREFERENCE = "CLIENT_SECURITY_PASSWORD_PREFERENCE";
    public static final String CLIENT_SECURITY_PASSWORD_VALUE_KEY = "CLIENT_SECURITY_PASSWORD_VALUE";
    public static final String CLIENT_SECURITY_PASSWORD_HINT_KEY = "CLIENT_SECURITY_PASSWORD_HINT";

    public static final String CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD_KEY = "CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD_KEY";
    public static final String CLIENT_MONEY_BITCOIN_WALLET_PASSWORD_KEY = "CLIENT_MONEY_BITCOIN_WALLET_PASSWORD_KEY";
    public static final String CLIENT_MONEY_PAYMON_WALLET_PASSWORD_KEY = "CLIENT_MONEY_PAYMON_WALLET_PASSWORD_KEY";

    public static boolean CLIENT_BASIC_DATE_FORMAT_IS_24H;
    public static boolean CLIENT_CONF_HIDE_EMAIL;
    public static boolean CLIENT_CONF_HIDE_PHONE;
    public static boolean CLIENT_CONF_HIDE_NAME;
    public static boolean CLIENT_MESSAGES_NOTIFY_IS_DONT_WORRY;
    public static boolean CLIENT_MESSAGES_NOTIFY_IS_VIBRATION;
    public static boolean CLIENT_MESSAGES_NOTIFY_NOTIFICATIONS_IS_ENABLED;
    public static boolean CLIENT_MESSAGES_NOTIFY_IS_TRANSACTIONS;
    public static boolean CLIENT_MESSAGES_NOTIFY_FRIENDS_INVITES;
    public static boolean CLIENT_MESSAGES_NOTIFY_GROUP_INVITES;
    public static boolean CLIENT_SECURITY_PASSWORD_IS_ENABLED;
    public static String CLIENT_SECURITY_PASSWORD_VALUE;
    public static String CLIENT_SECURITY_PASSWORD_HINT;

    public static String CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD;
    public static String CLIENT_MONEY_BITCOIN_WALLET_PASSWORD;
    public static String CLIENT_MONEY_PAYMON_WALLET_PASSWORD;
    //endregion

    public static void loadConfig() {
        SharedPreferences userPreferences = ApplicationLoader.applicationContext.getSharedPreferences(USER_CONFIG, Context.MODE_PRIVATE);

        String user = userPreferences.getString(USER, null);
        if (user != null) {
            byte[] userBytes = Base64.decode(user, Base64.DEFAULT);
            if (userBytes != null) {
                SerializedStream data = new SerializedStream(userBytes);
                RPC.UserObject deserialize = RPC.UserObject.deserialize(data, data.readInt32(false), true);
                if (deserialize instanceof RPC.PM_userSelf) {
                    currentUser = (RPC.PM_userSelf) deserialize;
                } else {
                    currentUser = null;
                    data.close();
                    return;
                }
                data.close();
            }
        }

        SharedPreferences clientPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.applicationContext);
        CLIENT_BASIC_DATE_FORMAT_IS_24H = clientPreferences.getString(CLIENT_BASIC_DATE_FORMAT_LIST, ApplicationLoader.applicationContext.getString(R.string.date_format_24h)).equals(ApplicationLoader.applicationContext.getString(R.string.date_format_24h));
        CLIENT_MESSAGES_NOTIFY_IS_DONT_WORRY = clientPreferences.getBoolean(CLIENT_MESSAGES_NOTIFY_DONT_WORRY_SWITCH, false);
        CLIENT_MESSAGES_NOTIFY_IS_VIBRATION = clientPreferences.getBoolean(CLIENT_MESSAGES_NOTIFY_VIBRATION_SWITCH, true);
        CLIENT_MESSAGES_NOTIFY_NOTIFICATIONS_IS_ENABLED = clientPreferences.getBoolean(CLIENT_MESSAGES_NOTIFY_NOTIFICATIONS_ENABLED_SWITCH, true);
        CLIENT_MESSAGES_NOTIFY_IS_TRANSACTIONS = clientPreferences.getBoolean(CLIENT_MESSAGES_NOTIFY_TRANSACTIONS_SWITCH, true);
        CLIENT_MESSAGES_NOTIFY_FRIENDS_INVITES = clientPreferences.getBoolean(CLIENT_MESSAGES_NOTIFY_FRIENDS_INVITES_SWITCH, true);
        CLIENT_MESSAGES_NOTIFY_GROUP_INVITES = clientPreferences.getBoolean(CLIENT_MESSAGES_NOTIFY_GROUP_INVITES_SWITCH, true);
        CLIENT_CONF_HIDE_EMAIL = clientPreferences.getBoolean(CLIENT_CONF_HIDE_EMAIL_SWITCH, false);
        CLIENT_CONF_HIDE_PHONE = clientPreferences.getBoolean(CLIENT_CONF_HIDE_PHONE_SWITCH, false);
        CLIENT_CONF_HIDE_NAME = clientPreferences.getBoolean(CLIENT_CONF_HIDE_NAME_SWITCH, false);
        CLIENT_SECURITY_PASSWORD_IS_ENABLED = clientPreferences.getBoolean(CLIENT_SECURITY_PASSWORD_ENABLED_CHECK, false);
        CLIENT_SECURITY_PASSWORD_VALUE = clientPreferences.getString(CLIENT_SECURITY_PASSWORD_VALUE_KEY, null);
        CLIENT_SECURITY_PASSWORD_HINT = clientPreferences.getString(CLIENT_SECURITY_PASSWORD_HINT_KEY, null);
        CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = clientPreferences.getString(CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD_KEY, null);
        CLIENT_MONEY_BITCOIN_WALLET_PASSWORD = clientPreferences.getString(CLIENT_MONEY_BITCOIN_WALLET_PASSWORD_KEY, null);
        CLIENT_MONEY_PAYMON_WALLET_PASSWORD = clientPreferences.getString(CLIENT_MONEY_PAYMON_WALLET_PASSWORD_KEY, null);

    }

    public static void saveConfig() {
        try {
            SharedPreferences userPreferences = ApplicationLoader.applicationContext.getSharedPreferences(USER_CONFIG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = userPreferences.edit();

            if (currentUser != null) {
                SerializedStream data = new SerializedStream();
                currentUser.serializeToStream(data);
                String userString = Base64.encodeToString(data.toByteArray(), Base64.DEFAULT);
                editor.putString(USER, userString);
                data.close();
            } else {
                editor.remove(USER);
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences clientPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.applicationContext);
        SharedPreferences.Editor editor = clientPreferences.edit();

        editor.putString(CLIENT_SECURITY_PASSWORD_VALUE_KEY, CLIENT_SECURITY_PASSWORD_VALUE);
        editor.putString(CLIENT_SECURITY_PASSWORD_HINT_KEY, CLIENT_SECURITY_PASSWORD_HINT);
        editor.putString(CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD_KEY, CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD);
        editor.putString(CLIENT_MONEY_BITCOIN_WALLET_PASSWORD_KEY, CLIENT_MONEY_BITCOIN_WALLET_PASSWORD);
        editor.putString(CLIENT_MONEY_PAYMON_WALLET_PASSWORD_KEY, CLIENT_MONEY_PAYMON_WALLET_PASSWORD);
        editor.apply();
    }

    public static void clearConfig() {
        currentUser = null;
        saveConfig();
    }

    public static void setDefaultConfig() {
        SharedPreferences clientPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.applicationContext);
        SharedPreferences.Editor editor = clientPreferences.edit();

        editor.putString(CLIENT_BASIC_DATE_FORMAT_LIST, ApplicationLoader.applicationContext.getString(R.string.date_format_24h));
        editor.putBoolean(CLIENT_MESSAGES_NOTIFY_DONT_WORRY_SWITCH, false);
        editor.putBoolean(CLIENT_MESSAGES_NOTIFY_VIBRATION_SWITCH, true);
        editor.putBoolean(CLIENT_MESSAGES_NOTIFY_NOTIFICATIONS_ENABLED_SWITCH, true);
        editor.putBoolean(CLIENT_MESSAGES_NOTIFY_TRANSACTIONS_SWITCH, true);
        editor.putBoolean(CLIENT_MESSAGES_NOTIFY_FRIENDS_INVITES_SWITCH, true);
        editor.putBoolean(CLIENT_MESSAGES_NOTIFY_GROUP_INVITES_SWITCH, true);
        editor.putBoolean(CLIENT_CONF_HIDE_EMAIL_SWITCH, false);
        editor.putBoolean(CLIENT_CONF_HIDE_PHONE_SWITCH, false);
        editor.putBoolean(CLIENT_CONF_HIDE_NAME_SWITCH, false);
        editor.putBoolean(CLIENT_SECURITY_PASSWORD_ENABLED_CHECK, false);
        editor.putString(CLIENT_SECURITY_PASSWORD_VALUE_KEY, null);
        editor.putString(CLIENT_SECURITY_PASSWORD_HINT_KEY, null);
        editor.apply();
    }
}
