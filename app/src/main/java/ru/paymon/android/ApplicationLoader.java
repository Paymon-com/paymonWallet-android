package ru.paymon.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.utils.KeyGenerator;
import ru.paymon.android.utils.cache.lruramcache.LruRamCache;


public class ApplicationLoader extends Application {
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;

    @SuppressWarnings("JniMissingFunction")
    private static native int native_init(String host, short port, int version);

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());

        User.loadConfig();

        System.loadLibrary("paymon-lib");
        KeyGenerator.getInstance();
        native_init(Config.HOST, Config.PORT, Config.VERSION);
        final Intent connectorIntent = new Intent(ApplicationLoader.applicationContext, ConnectorService.class);
        ApplicationLoader.applicationContext.startService(connectorIntent);
    }

    public static void finish() {
        LruRamCache.getInstance().lruCache.evictAll();
        MessagesManager.getInstance().dispose();
        MediaManager.getInstance().dispose();
        UsersManager.getInstance().dispose();
        GroupsManager.getInstance().dispose();
    }
}
