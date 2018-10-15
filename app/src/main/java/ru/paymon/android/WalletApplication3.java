//package ru.paymon.android;
//
//import android.app.ActivityManager;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.media.AudioAttributes;
//import android.media.AudioManager;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Looper;
//import android.preference.PreferenceManager;
//import android.support.annotation.MainThread;
//import android.support.annotation.WorkerThread;
//import android.support.multidex.MultiDexApplication;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//import com.google.common.base.Splitter;
//import com.google.common.base.Stopwatch;
//import com.google.common.collect.ImmutableList;
//import com.google.common.util.concurrent.SettableFuture;
//
//import org.bitcoinj.core.Transaction;
//import org.bitcoinj.core.VerificationException;
//import org.bitcoinj.core.VersionMessage;
//import org.bitcoinj.crypto.LinuxSecureRandom;
//import org.bitcoinj.crypto.MnemonicCode;
//import org.bitcoinj.utils.Threading;
//import org.bitcoinj.wallet.UnreadableWalletException;
//import org.bitcoinj.wallet.Wallet;
//import org.bitcoinj.wallet.WalletFiles;
//import org.bitcoinj.wallet.WalletProtobufSerializer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import ru.paymon.android.gateway.bitcoin.Configuration;
//import ru.paymon.android.gateway.bitcoin.Constants;
//import ru.paymon.android.gateway.bitcoin.service.BlockchainService;
//import ru.paymon.android.gateway.bitcoin.util.CrashReporter;
//import ru.paymon.android.gateway.bitcoin.util.Toast;
//import ru.paymon.android.gateway.bitcoin.util.WalletUtils;
//
//public class WalletApplication extends MultiDexApplication {
//    private ActivityManager activityManager;
//
//    private File walletFile;
//    private WalletFiles walletFiles;
//    private Configuration config;
//
//    public static final String ACTION_WALLET_REFERENCE_CHANGED = WalletApplication.class.getPackage().getName() + ".wallet_reference_changed";
//
//    public static final long TIME_CREATE_APPLICATION = System.currentTimeMillis();
//    private static final String BIP39_WORDLIST_FILENAME = "bip39-wordlist.txt";
//
//    private static final Logger log = LoggerFactory.getLogger(WalletApplication.class);
//
//    @Override
//    public void onCreate() {
//        new LinuxSecureRandom();
//
//        Threading.throwOnLockCycles();
//        org.bitcoinj.core.Context.enableStrictMode();
//        org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
//
//        super.onCreate();
//
//        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//
//        walletFile = getFileStreamPath(Constants.Files.WALLET_FILENAME_PROTOBUF);
//
//        cleanupFiles();
//
//        initNotificationManager();
//    }
//
//    public synchronized Configuration getConfiguration() {
//        if (config == null)
//            config = new Configuration(PreferenceManager.getDefaultSharedPreferences(this), getResources());
//        return config;
//    }
//
//    @MainThread
//    public Wallet getWallet() {
//        final Stopwatch watch = Stopwatch.createStarted();
//        final SettableFuture<Wallet> future = SettableFuture.create();
//        getWalletAsync(wallet -> future.set(wallet));
//        try {
//            return future.get();
//        } catch (final InterruptedException | ExecutionException x) {
//            throw new RuntimeException(x);
//        } finally {
//            watch.stop();
//            if (Looper.myLooper() == Looper.getMainLooper())
//                log.warn("UI thread blocked for " + watch + " when using getWallet()", new RuntimeException());
//        }
//    }
//
//    private final Executor getWalletExecutor = Executors.newSingleThreadExecutor();
//    private final Object getWalletLock = new Object();
//
//    @MainThread
//    public void getWalletAsync(final OnWalletLoadedListener listener) {
//        getWalletExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
//                synchronized (getWalletLock) {
//                    initMnemonicCode();
//                    if (walletFiles == null)
//                        loadWalletFromProtobuf();
//                }
//                if(walletFiles != null) {
//                    Wallet wallet = walletFiles.getWallet();
//                    if (wallet != null)
//                        listener.onWalletLoaded(wallet);
//                }
//            }
//
//            @WorkerThread
//            private void loadWalletFromProtobuf() {
//                Wallet wallet;
//                if (walletFile.exists()) {
//                    try (final FileInputStream walletStream = new FileInputStream(walletFile)) {
//                        final Stopwatch watch = Stopwatch.createStarted();
//                        wallet = new WalletProtobufSerializer().readWallet(walletStream);
//                        watch.stop();
//
//                        if (!wallet.getParams().equals(Constants.NETWORK_PARAMETERS))
//                            throw new UnreadableWalletException("bad wallet network parameters: " + wallet.getParams().getId());
//
//                        Log.e("AAA", "wallet from " + walletFile);
//                        log.info("wallet loaded from: '{}', took {}", walletFile, watch);
//                    } catch (final IOException | UnreadableWalletException x) {
//                        log.warn("problem loading wallet, auto-restoring: " + walletFile, x);
//                        wallet = WalletUtils.restoreWalletFromAutoBackup(WalletApplication.this);
//                        if (wallet != null)
//                            new Toast(WalletApplication.this).postLongToast(R.string.toast_wallet_reset);
//                    }
//                    if (!wallet.isConsistent()) {
//                        log.warn("inconsistent wallet, auto-restoring: " + walletFile);
//                        wallet = WalletUtils.restoreWalletFromAutoBackup(WalletApplication.this);
//                        if (wallet != null)
//                            new Toast(WalletApplication.this).postLongToast(R.string.toast_wallet_reset);
//                    }
//
//                    if (!wallet.getParams().equals(Constants.NETWORK_PARAMETERS))
//                        throw new Error("bad wallet network parameters: " + wallet.getParams().getId());
//
//                    wallet.cleanup();
//                    walletFiles = wallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS, TimeUnit.MILLISECONDS, null);
//                } else {
//
//                    final Stopwatch watch = Stopwatch.createStarted();
//                    wallet = new Wallet(Constants.NETWORK_PARAMETERS);
//                    walletFiles = wallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS, TimeUnit.MILLISECONDS, null);
//                    autosaveWalletNow(); // persist...
//                    WalletUtils.autoBackupWallet(WalletApplication.this, wallet); // ...and backup asap
//                    watch.stop();
//                    log.info("fresh wallet created, took {}", watch);
//                    Log.e("AAA", "wallet created " );
//                    config.armBackupReminder();
//                }
//            }
//
//            private void initMnemonicCode() {
//                if (MnemonicCode.INSTANCE == null) {
//                    try {
//                        final Stopwatch watch = Stopwatch.createStarted();
//                        MnemonicCode.INSTANCE = new MnemonicCode(getAssets().open(BIP39_WORDLIST_FILENAME), null);
//                        watch.stop();
//                        log.info("BIP39 wordlist loaded from: '{}', took {}", BIP39_WORDLIST_FILENAME, watch);
//                    } catch (final IOException x) {
//                        throw new Error(x);
//                    }
//                }
//            }
//        });
//    }
//
//    public Wallet createWallet() {
//        final Stopwatch watch = Stopwatch.createStarted();
//        Wallet wallet = new Wallet(Constants.NETWORK_PARAMETERS);
//        walletFiles = wallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS, TimeUnit.MILLISECONDS, null);
//        autosaveWalletNow(); // persist...
//        WalletUtils.autoBackupWallet(WalletApplication.this, wallet); // ...and backup asap
//        watch.stop();
//        log.info("fresh wallet created, took {}", watch);
//        Log.e("AAA", "wallet created ");
//        config.armBackupReminder();
//        return wallet;
//    }
//
//    public interface OnWalletLoadedListener {
//        void onWalletLoaded(Wallet wallet);
//    }
//
//    public void autosaveWalletNow() {
//        final Stopwatch watch = Stopwatch.createStarted();
//        synchronized (getWalletLock) {
//            if (walletFiles != null) {
//                watch.stop();
//                log.info("wallet saved to: '{}', took {}", walletFile, watch);
//                try {
//                    walletFiles.saveNow();
//                } catch (final IOException x) {
//                    log.warn("problem with forced autosaving of wallet", x);
//                    CrashReporter.saveBackgroundTrace(x, packageInfo);
//                }
//            }
//        }
//    }
//
//    public void replaceWallet(final Wallet newWallet) {
//        newWallet.cleanup();
//        BlockchainService.resetBlockchain(this);
//
//        final Wallet oldWallet = getWallet();
//        synchronized (getWalletLock) {
//            oldWallet.shutdownAutosaveAndWait(); // this will also prevent BlockchainService to save
//            walletFiles = newWallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS,
//                    TimeUnit.MILLISECONDS, null);
//        }
//        config.maybeIncrementBestChainHeightEver(newWallet.getLastBlockSeenHeight());
//        WalletUtils.autoBackupWallet(this, newWallet);
//
//        final Intent broadcast = new Intent(ACTION_WALLET_REFERENCE_CHANGED);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
//    }
//
//    private void cleanupFiles() {
//        for (final String filename : fileList()) {
//            if (filename.startsWith(Constants.Files.WALLET_KEY_BACKUP_BASE58)
//                    || filename.startsWith(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF + '.')
//                    || filename.endsWith(".tmp")) {
//                final File file = new File(getFilesDir(), filename);
//                log.info("removing obsolete file: '{}'", file);
//                file.delete();
//            }
//        }
//    }
//
//    private void initNotificationManager() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            final Stopwatch watch = Stopwatch.createStarted();
//            final android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            final NotificationChannel received = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_RECEIVED, getString(R.string.notification_channel_received_name), android.app.NotificationManager.IMPORTANCE_DEFAULT);
//            received.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.coins_received),
//                    new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
//                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build());
//            nm.createNotificationChannel(received);
//
//            final NotificationChannel ongoing = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_ONGOING, getString(R.string.notification_channel_ongoing_name), android.app.NotificationManager.IMPORTANCE_LOW);
//            nm.createNotificationChannel(ongoing);
//
//            final NotificationChannel important = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_IMPORTANT, getString(R.string.notification_channel_important_name), NotificationManager.IMPORTANCE_HIGH);
//            nm.createNotificationChannel(important);
//
//            log.info("created notification channels, took {}", watch);
//        }
//    }
//
//    public void processDirectTransaction(final Transaction tx) throws VerificationException {
//        final Wallet wallet = getWallet();
//        if (wallet.isTransactionRelevant(tx)) {
//            wallet.receivePending(tx, null);
//            BlockchainService.broadcastTransaction(this, tx);
//        }
//    }
//
//    private PackageInfo packageInfo;
//
//    public synchronized PackageInfo packageInfo() {
//        // replace by BuildConfig.VERSION_* as soon as it's possible
//        if (packageInfo == null) {
//            try {
//                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            } catch (final PackageManager.NameNotFoundException x) {
//                throw new RuntimeException(x);
//            }
//        }
//        return packageInfo;
//    }
//
//    public final String applicationPackageFlavor() {
//        final String packageName = getPackageName();
//        final int index = packageName.lastIndexOf('_');
//
//        if (index != -1)
//            return packageName.substring(index + 1);
//        else
//            return null;
//    }
//
//    public static String httpUserAgent(final String versionName) {
//        final VersionMessage versionMessage = new VersionMessage(Constants.NETWORK_PARAMETERS, 0);
//        versionMessage.appendToSubVer(Constants.USER_AGENT, versionName, null);
//        return versionMessage.subVer;
//    }
//
//    public String httpUserAgent() {
//        return httpUserAgent(packageInfo().versionName);
//    }
//
//    public int maxConnectedPeers() {
//        return activityManager.isLowRamDevice() ? 4 : 6;
//    }
//
//    public int scryptIterationsTarget() {
//        return activityManager.isLowRamDevice() ? Constants.SCRYPT_ITERATIONS_TARGET_LOWRAM : Constants.SCRYPT_ITERATIONS_TARGET;
//    }
//
//    public static String versionLine(final PackageInfo packageInfo) {
//        return ImmutableList.copyOf(Splitter.on('.').splitToList(packageInfo.packageName)).reverse().get(0) + ' ' + packageInfo.versionName + (BuildConfig.DEBUG ? " (debuggable)" : "");
//    }
//}
