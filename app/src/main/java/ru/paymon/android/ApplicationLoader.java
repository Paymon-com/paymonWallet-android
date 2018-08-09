package ru.paymon.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.StrictMode;

//import ru.paymon.android.broadcastreceivers.NetworkStateReceiver;
import ru.paymon.android.broadcastreceivers.NetworkStateReceiver;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.utils.KeyGenerator;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.utils.cache.lruramcache.LruRamCache;


public class ApplicationLoader extends Application {
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    public static volatile SQLiteDatabase db;

    @SuppressWarnings("JniMissingFunction")
    private static native int native_init(String host, short port, int version);

    static {
        System.loadLibrary("paymon-lib");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());

        Utils.stageQueue.postRunnable(()->{
            DBHelper dbHelper = new DBHelper(applicationContext);
            try{
                db = dbHelper.getWritableDatabase();
            }catch (Exception e){
                db = dbHelper.getReadableDatabase();
            }

            User.loadConfig();
        });

        NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ApplicationLoader.applicationContext.registerReceiver(networkStateReceiver, filter);

        Utils.checkDisplaySize(getApplicationContext(), null);
        Utils.maxSize = Utils.displaySize.x - Utils.displaySize.x / 100.0 * 45;


        KeyGenerator.getInstance();
        native_init(Config.HOST, Config.PORT, Config.VERSION);

        Intent connectorIntent = new Intent(applicationContext, ConnectorService.class);
        startService(connectorIntent);
        NetworkManager.getInstance().bindServices();
    }

    public static void finish() {
        LruRamCache.getInstance().lruCache.evictAll();
        MessagesManager.getInstance().dispose();
        MediaManager.getInstance().dispose();
        UsersManager.getInstance().dispose();
        GroupsManager.getInstance().dispose();
    }

    private static StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
    public static void initStrictMode(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskReads()
                .detectNetwork()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build();
        StrictMode.setThreadPolicy(policy);
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy().Builder()
//                .dete);
    }

    public static void setOldStrictMode(){
        StrictMode.setThreadPolicy(old);
    }
}
