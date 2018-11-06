package ru.paymon.android;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.rxpermission.RealRxPermission;
import com.vanniktech.rxpermission.RxPermission;

import java.util.concurrent.Executors;

import cat.ereza.logcatreporter.LogcatReporter;
import io.fabric.sdk.android.Fabric;
import ru.paymon.android.broadcastreceivers.NetworkStateReceiver;
import ru.paymon.android.emoji.CustomEmojiProvider;
import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.room.AppDatabase;
import ru.paymon.android.utils.KeyGenerator;


public class ApplicationLoader extends WalletApplication {
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    public static RxPermission rxPermission;

    @SuppressWarnings("JniMissingFunction")
    private static native int native_init(String host, short port, int version);

    static {
        System.loadLibrary("paymon-lib");
    }

    @Override
    public void onCreate() {
        Fabric.with(this, new Crashlytics());
        LogcatReporter.install();

        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());

        User.loadConfig();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel messagesChannel = new NotificationChannel(Config.MESSAGES_NOTIFICATION_CHANNEL_ID, Config.MESSAGES_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            if (User.CLIENT_MESSAGES_NOTIFY_IS_VIBRATION)
                messagesChannel.setVibrationPattern(new long[]{100, 200, 100, 300});
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            messagesChannel.setSound(soundUri, attributes);
            ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(messagesChannel);
        }

        rxPermission = RealRxPermission.getInstance(this);

        super.onCreate();

        Executors.newSingleThreadExecutor().submit(() -> {
            Picasso.setSingletonInstance(new Picasso.Builder(this).downloader(new OkHttp3Downloader(getCacheDir(), 500000000)).build());
            EmojiManager.install(new CustomEmojiProvider());

            IntentFilter networkIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            ApplicationLoader.applicationContext.registerReceiver(new NetworkStateReceiver(), networkIntentFilter);

            KeyGenerator.getInstance();
            native_init(Config.HOST, Config.PORT, Config.VERSION);

            startService(new Intent(applicationContext, ConnectorService.class));
            NetworkManager.getInstance().bindServices();
        });
    }

    //    private static StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
//
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
//
//    public static void setOldStrictMode() {
//        StrictMode.setThreadPolicy(old);
//    }

//    private boolean isConnectorServiceRunning() {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (ConnectorService.class.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }
}
