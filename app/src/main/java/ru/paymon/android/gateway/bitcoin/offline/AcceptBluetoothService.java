//package ru.paymon.android.gateway.bitcoin.offline;
//
//
//import static android.support.v4.util.Preconditions.checkNotNull;
//
//import java.io.IOException;
//
//import org.bitcoinj.core.Transaction;
//import org.bitcoinj.core.VerificationException;
//import org.bitcoinj.wallet.Wallet;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//
//import android.arch.lifecycle.LifecycleService;
//import android.arch.lifecycle.Observer;
//import android.bluetooth.BluetoothAdapter;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
//import android.text.format.DateUtils;
//
//import ru.paymon.android.R;
//import ru.paymon.android.WalletApplication;
//import ru.paymon.android.gateway.bitcoin.data.WalletLiveData;
//import ru.paymon.android.gateway.bitcoin.service.BlockchainService;
//import ru.paymon.android.gateway.bitcoin.util.CrashReporter;
//import ru.paymon.android.gateway.bitcoin.util.Toast;
//
///**
// * @author Andreas Schildbach
// */
//public final class AcceptBluetoothService extends LifecycleService {
//    private WalletApplication application;
//    private WalletLiveData wallet;
//    private WakeLock wakeLock;
//    private AcceptBluetoothThread classicThread;
//    private AcceptBluetoothThread paymentProtocolThread;
//
//    private long serviceCreatedAt;
//
//    private final Handler handler = new Handler();
//
//    private static final long TIMEOUT_MS = 5 * DateUtils.MINUTE_IN_MILLIS;
//
//    private static final Logger log = LoggerFactory.getLogger(AcceptBluetoothService.class);
//
//    @Override
//    public IBinder onBind(final Intent intent) {
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(final Intent intent, final int flags, final int startId) {
//        super.onStartCommand(intent, flags, startId);
//
//        handler.removeCallbacks(timeoutRunnable);
//        handler.postDelayed(timeoutRunnable, TIMEOUT_MS);
//
//        return START_NOT_STICKY;
//    }
//
//    @Override
//    public void onCreate() {
//        serviceCreatedAt = System.currentTimeMillis();
//        log.debug(".onCreate()");
//
//        super.onCreate();
//        this.application = (WalletApplication) getApplication();
//        final BluetoothAdapter bluetoothAdapter = checkNotNull(BluetoothAdapter.getDefaultAdapter());
//        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//
//        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
//        wakeLock.acquire();
//
//        registerReceiver(bluetoothStateChangeReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
//
//        try {
//            classicThread = new AcceptBluetoothThread.ClassicBluetoothThread(bluetoothAdapter) {
//                @Override
//                public boolean handleTx(final Transaction tx) {
//                    return AcceptBluetoothService.this.handleTx(tx);
//                }
//            };
//            paymentProtocolThread = new AcceptBluetoothThread.PaymentProtocolThread(bluetoothAdapter) {
//                @Override
//                public boolean handleTx(final Transaction tx) {
//                    return AcceptBluetoothService.this.handleTx(tx);
//                }
//            };
//        } catch (final IOException x) {
//            new Toast(this).longToast(R.string.error_bluetooth, x.getMessage());
//            log.warn("problem with listening, stopping service", x);
//            CrashReporter.saveBackgroundTrace(x, application.packageInfo());
//            stopSelf();
//        }
//
//        wallet = new WalletLiveData(application);
//        wallet.observe(this, new Observer<Wallet>() {
//            @Override
//            public void onChanged(final Wallet wallet) {
//                classicThread.start();
//                paymentProtocolThread.start();
//            }
//        });
//    }
//
//    private boolean handleTx(final Transaction tx) {
//        log.info("tx " + tx.getHashAsString() + " arrived via blueooth");
//
//        final Wallet wallet = this.wallet.getValue();
//        try {
//            if (wallet.isTransactionRelevant(tx)) {
//                wallet.receivePending(tx, null);
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        BlockchainService.broadcastTransaction(AcceptBluetoothService.this, tx);
//                    }
//                });
//            } else {
//                log.info("tx " + tx.getHashAsString() + " irrelevant");
//            }
//
//            return true;
//        } catch (final VerificationException x) {
//            log.info("cannot verify tx " + tx.getHashAsString() + " received via bluetooth", x);
//        }
//
//        return false;
//    }
//
//    @Override
//    public void onDestroy() {
//        if (paymentProtocolThread != null)
//            paymentProtocolThread.stopAccepting();
//        if (classicThread != null)
//            classicThread.stopAccepting();
//
//        unregisterReceiver(bluetoothStateChangeReceiver);
//
//        wakeLock.release();
//
//        handler.removeCallbacksAndMessages(null);
//
//        super.onDestroy();
//
//        log.info("service was up for " + ((System.currentTimeMillis() - serviceCreatedAt) / 1000 / 60) + " minutes");
//    }
//
//    private final BroadcastReceiver bluetoothStateChangeReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(final Context context, final Intent intent) {
//            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
//
//            if (state == BluetoothAdapter.STATE_TURNING_OFF || state == BluetoothAdapter.STATE_OFF) {
//                log.info("bluetooth was turned off, stopping service");
//
//                stopSelf();
//            }
//        }
//    };
//
//    private final Runnable timeoutRunnable = new Runnable() {
//        @Override
//        public void run() {
//            log.info("timeout expired, stopping service");
//
//            stopSelf();
//        }
//    };
//}
