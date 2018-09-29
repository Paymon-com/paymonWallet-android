package ru.paymon.android;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.StrictMode;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;

import ru.paymon.android.broadcastreceivers.NetworkStateReceiver;
import ru.paymon.android.emoji.CustomEmojiProvider;
import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.test.AppDatabase;
import ru.paymon.android.utils.KeyGenerator;
import ru.paymon.android.utils.Utils;


public class ApplicationLoader extends Application {
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
//    public static volatile SQLiteDatabase db;
    public static volatile AppDatabase db;

    @SuppressWarnings("JniMissingFunction")
    private static native int native_init(String host, short port, int version);

    static {
        System.loadLibrary("paymon-lib");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Picasso.setSingletonInstance(new Picasso.Builder(this).downloader(new OkHttp3Downloader(getCacheDir(), 500000000))/*.indicatorsEnabled(true)*/.build());


        EmojiManager.install(new CustomEmojiProvider());
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());

        Utils.stageQueue.postRunnable(() -> {
             db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database").allowMainThreadQueries().build();
//            DBHelper dbHelper = new DBHelper(applicationContext);
//            try{
//                db = dbHelper.getWritableDatabase();
//            }catch (Exception e){
//                db = dbHelper.getReadableDatabase();
//            }

            User.loadConfig();
        });

        NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();
        IntentFilter networkIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ApplicationLoader.applicationContext.registerReceiver(networkStateReceiver, networkIntentFilter);

        Utils.checkDisplaySize(getApplicationContext(), null);
        Utils.maxSize = Utils.displaySize.x - Utils.displaySize.x / 100.0 * 45;

        KeyGenerator.getInstance();
        native_init(Config.HOST, Config.PORT, Config.VERSION);

        Intent connectorIntent = new Intent(applicationContext, ConnectorService.class);
        startService(connectorIntent);
        NetworkManager.getInstance().bindServices();
    }

    public static void finish() {
        MessagesManager.getInstance().dispose();
//        MediaManager.getInstance().dispose();
        UsersManager.getInstance().dispose();
        GroupsManager.getInstance().dispose();
    }

    private static StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();

    public static void initStrictMode() {
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

    public static void setOldStrictMode() {
        StrictMode.setThreadPolicy(old);
    }
}
