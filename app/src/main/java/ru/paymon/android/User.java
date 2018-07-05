package ru.paymon.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;

import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.SerializedStream;

public class User {
    public static RPC.PM_userFull currentUser;

    public static boolean notificationCheckWorry;
    public static boolean notificationCheckVibration;
    public static Uri notificationRingtone;

    public static void loadConfig() {
        SharedPreferences userPreferences = ApplicationLoader.applicationContext.getSharedPreferences("user_config", Context.MODE_PRIVATE);

        String user = userPreferences.getString("user", null);
        if (user != null) {
            byte[] userBytes = Base64.decode(user, Base64.DEFAULT);
            if (userBytes != null) {
                SerializedStream data = new SerializedStream(userBytes);
                RPC.UserObject deserialize = RPC.UserObject.deserialize(data, data.readInt32(false), true);
                if (deserialize instanceof RPC.PM_userFull) {
                    currentUser = (RPC.PM_userFull) deserialize;
                } else {
                    currentUser = null;
                    data.close();
                    return;
                }
                data.close();
            }
        }

        SharedPreferences clientPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.applicationContext);
        //TODO:
    }

    public static void saveConfig() {
        try {
            SharedPreferences userPreferences = ApplicationLoader.applicationContext.getSharedPreferences("user_config", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = userPreferences.edit();

            if (currentUser != null) {
                SerializedStream data = new SerializedStream();
                currentUser.serializeToStream(data);
                String userString = Base64.encodeToString(data.toByteArray(), Base64.DEFAULT);
                editor.putString("user", userString);
                data.close();
            } else {
                editor.remove("user");
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
