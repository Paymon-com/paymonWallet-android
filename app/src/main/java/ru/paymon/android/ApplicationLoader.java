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
import com.vanniktech.rxpermission.RealRxPermission;
import com.vanniktech.rxpermission.RxPermission;

import ru.paymon.android.broadcastreceivers.NetworkStateReceiver;
import ru.paymon.android.emoji.CustomEmojiProvider;
import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.room.AppDatabase;
import ru.paymon.android.utils.KeyGenerator;


public class ApplicationLoader extends Application {
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    //    public static volatile SQLiteDatabase db;
    public static volatile AppDatabase db;
    public static RxPermission rxPermission;;

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

        rxPermission = RealRxPermission.getInstance(this);

        User.loadConfig();
        Picasso.setSingletonInstance(new Picasso.Builder(this).downloader(new OkHttp3Downloader(getCacheDir(), 500000000))/*.indicatorsEnabled(true)*/.build());
        EmojiManager.install(new CustomEmojiProvider());
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database").allowMainThreadQueries().build();

        IntentFilter networkIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ApplicationLoader.applicationContext.registerReceiver(new NetworkStateReceiver(), networkIntentFilter);

        KeyGenerator.getInstance();
        native_init(Config.HOST, Config.PORT, Config.VERSION);

        startService(new Intent(applicationContext, ConnectorService.class));
        NetworkManager.getInstance().bindServices();

        initStrictMode();
    }

    public static void finish() {
//        MessagesManager.getInstance().dispose();
//        MediaManager.getInstance().dispose();
//        UsersManager.getInstance().dispose();
//        GroupsManager.getInstance().dispose();
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
    }

    public static void setOldStrictMode() {
        StrictMode.setThreadPolicy(old);
    }
}
