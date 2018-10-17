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

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.rxpermission.RealRxPermission;
import com.vanniktech.rxpermission.RxPermission;

import ru.paymon.android.broadcastreceivers.NetworkStateReceiver;
import ru.paymon.android.emoji.CustomEmojiProvider;
import ru.paymon.android.gateway.bitcoin.service.BlockchainService;
import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.room.AppDatabase;
import ru.paymon.android.utils.KeyGenerator;


public class ApplicationLoader extends WalletApplication {
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    public static volatile AppDatabase db;
    public static RxPermission rxPermission;

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

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel messagesChannel = new NotificationChannel(Config.MESSAGES_NOTIFICATION_CHANNEL_ID, Config.MESSAGES_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
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

        User.loadConfig();
        Picasso.setSingletonInstance(new Picasso.Builder(this).downloader(new OkHttp3Downloader(getCacheDir(), 500000000)).build());
        EmojiManager.install(new CustomEmojiProvider());
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database").allowMainThreadQueries().build();//TODO:delete allow main thread queries

        IntentFilter networkIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ApplicationLoader.applicationContext.registerReceiver(new NetworkStateReceiver(), networkIntentFilter);

        KeyGenerator.getInstance();
        native_init(Config.HOST, Config.PORT, Config.VERSION);

        if(isConnectorServiceRunning()){
            stopService(new Intent(applicationContext, ConnectorService.class));
        }
        startService(new Intent(applicationContext, ConnectorService.class));
        NetworkManager.getInstance().bindServices();

        BlockchainService.start(this, true);
//
//        rxPermission
//                .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Permission>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(Permission permission) {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//                if (ApplicationLoader.rxPermission.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && ApplicationLoader.rxPermission.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
////                    Executors.newSingleThreadExecutor().submit(() -> {
////                        test();
////                        initBtcBlockchaine(new String[]{"muY4qKejpNVhih8TMycMUq1rxUaiYZkfcX", "testnet"});
////                    });
//                }
//            }
//        });
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

//    private void initBtcBlockchaine(String[] args) {
//        BriefLogFormatter.init();
//        if (args.length < 1) {
//            System.err.println("Usage: address-to-send-back-to [regtest|testnet]");
//            return;
//        }
//
//        // Figure out which network we should connect to. Each one gets its own set of files.
//        NetworkParameters params;
//        String filePrefix;
//        if (args.length > 1 && args[1].equals("testnet")) {
//            params = TestNet3Params.get();
//            filePrefix = "forwarding-service-testnet";
//        } else if (args.length > 1 && args[1].equals("regtest")) {
//            params = RegTestParams.get();
//            filePrefix = "forwarding-service-regtest";
//        } else {
//            params = MainNetParams.get();
//            filePrefix = "forwarding-service";
//        }
//        // Parse the address given as the first parameter.
//
//        forwardingAddress = Address.fromBase58(params, args[0]);
//
//        Log.e("AAA", "Network: " + params.getId());
//        Log.e("AAA", "Forwarding address: " + forwardingAddress);
//
//        // Start up a basic app using a class that automates some boilerplate.
//        kit = new WalletAppKit(params, new File(getCacheDir().getPath()), filePrefix);
//
//        if (params == RegTestParams.get()) {
//            // Regression test mode is designed for testing and development only, so there's no public network for it.
//            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
//            kit.connectToLocalHost();
//        }
//
//        // Download the block chain and wait until it's done.
//        kit.startAsync();
//        kit.awaitRunning();
//
//        Log.e("AAA", kit.wallet().getBalance().value + " sss");
//
//        // We want to know when we receive money.
//        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
//            @Override
//            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
//                // Runs in the dedicated "user thread" (see bitcoinj docs for more info on this).
//                //
//                // The transaction "tx" can either be pending, or included into a block (we didn't see the broadcast).
//                Coin value = tx.getValueSentToMe(wallet);
//                Log.e("AAA", "Received tx for " + value.toFriendlyString() + ": " + tx);
//                Log.e("AAA", "Transaction will be forwarded after it confirms.");
//                // Wait until it's made it into the block chain (may run immediately if it's already there).
//                //
//                // For this dummy app of course, we could just forward the unconfirmed transaction. If it were
//                // to be double spent, no harm done. Wallet.allowSpendingUnconfirmedTransactions() would have to
//                // be called in onSetupCompleted() above. But we don't do that here to demonstrate the more common
//                // case of waiting for a block.
//                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>() {
//                    @Override
//                    public void onSuccess(TransactionConfidence result) {
//                        Log.e("AAA", "Confirmation received.");
//                        forwardCoins(tx);
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        // This kind of future can't fail, just rethrow in case something weird happens.
//                        throw new RuntimeException(t);
//                    }
//                });
//            }
//        });
////        Address sendToAddress = Address.fromBase58(params, kit.wallet().currentReceiveKey().serializePubB58(params));
////        Log.e("AAA", "Send coins to: " + sendToAddress);
////        Log.e("AAA", "Waiting for coins to arrive. Press Ctrl-C to quit.");
//
//        try {
//            Thread.sleep(Long.MAX_VALUE);
//        } catch (InterruptedException ignored) {
//        }
//    }
//
//    private void forwardCoins(Transaction tx) {
//        try {
//            // Now send the coins onwards.
//            SendRequest sendRequest = SendRequest.emptyWallet(forwardingAddress);
//            Wallet.SendResult sendResult = kit.wallet().sendCoins(sendRequest);
//            checkNotNull(sendResult);  // We should never try to send more coins than we have!
//            Log.e("AAA", "Sending ...");
//            // Register a callback that is invoked when the transaction has propagated across the network.
//            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
//            // need access to the object the future returns.
//            sendResult.broadcastComplete.addListener(new Runnable() {
//                @Override
//                public void run() {
//                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
//                    Log.e("AAA", "Sent coins onwards! Transaction hash is " + sendResult.tx.getHashAsString());
//                }
//            }, MoreExecutors.directExecutor());
//        } catch (KeyCrypterException | InsufficientMoneyException e) {
//            // We don't use encrypted wallets in this example - can never happen.
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void test() {
//
//        // First we configure the network we want to use.
//        // The available options are:
//        // - MainNetParams
//        // - TestNet3Params
//        // - RegTestParams
//        // While developing your application you probably want to use the Regtest mode and run your local bitcoin network. Run bitcoind with the -regtest flag
//        // To test you app with a real network you can use the testnet. The testnet is an alternative bitcoin network that follows the same rules as main network. Coins are worth nothing and you can get coins for example from http://faucet.xeno-genesis.com/
//        //
//        // For more information have a look at: https://bitcoinj.github.io/testing and https://bitcoin.org/en/developer-examples#testing-applications
//        NetworkParameters params = TestNet3Params.get();
//
//        // Now we initialize a new WalletAppKit. The kit handles all the boilerplate for us and is the easiest way to get everything up and running.
//        // Have a look at the WalletAppKit documentation and its source to understand what's happening behind the scenes: https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/kits/WalletAppKit.java
//        WalletAppKit kit = new WalletAppKit(params, new File(getCacheDir().getPath()), "walletappkit1-example");
//
//        // In case you want to connect with your local bitcoind tell the kit to connect to localhost.
//        // You must do that in reg test mode.
//        //kit.connectToLocalHost();
//
//        // Now we start the kit and sync the blockchain.
//        // bitcoinj is working a lot with the Google Guava libraries. The WalletAppKit extends the AbstractIdleService. Have a look at the introduction to Guava services: https://github.com/google/guava/wiki/ServiceExplained
//        Log.e("AAA", "before start");
//        kit.startAsync();
//        Log.e("AAA", "after start");
//        kit.awaitRunning();
//        Log.e("AAA", "after await");
//
//
//        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
//            @Override
//            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
//                Log.e("AAA", "-----> coins resceived: " + tx.getHashAsString());
//                Log.e("AAA", "received: " + tx.getValue(wallet));
//            }
//        });
//
//        kit.wallet().addCoinsSentEventListener(new WalletCoinsSentEventListener() {
//            @Override
//            public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
//                Log.e("AAA", "coins sent");
//            }
//        });
//
//        kit.wallet().addKeyChainEventListener(new KeyChainEventListener() {
//            @Override
//            public void onKeysAdded(List<ECKey> keys) {
//                Log.e("AAA", "new key added");
//            }
//        });
//
//        kit.wallet().addScriptsChangeEventListener(new ScriptsChangeEventListener() {
//            @Override
//            public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
//                Log.e("AAA", "new script added");
//            }
//        });
//
//        kit.wallet().addTransactionConfidenceEventListener(new TransactionConfidenceEventListener() {
//            @Override
//            public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
//                Log.e("AAA", "-----> confidence changed: " + tx.getHashAsString());
//                TransactionConfidence confidence = tx.getConfidence();
//                Log.e("AAA", "new block depth: " + confidence.getDepthInBlocks());
//            }
//        });
//
//        // Ready to run. The kit syncs the blockchain and our wallet event listener gets notified when something happens.
//        // To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
//        Log.e("AAA", "send money to: " + kit.wallet().freshReceiveAddress().toString());
//
//        // Make sure to properly shut down all the running services when you manually want to stop the kit. The WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our application is stopping.
//        //Log.e("AAA", "shutting down again");
//        //kit.stopAsync();
//        //kit.awaitTerminated();
//    }

    private boolean isConnectorServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ConnectorService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
