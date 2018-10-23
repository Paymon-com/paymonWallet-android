package ru.paymon.android;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.toolbox.Volley;
import com.google.common.base.Charsets;

import org.apache.commons.io.IOUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.VersionMessage;
import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.paymon.android.gateway.bitcoin.Configuration;
import ru.paymon.android.gateway.bitcoin.Constants;
import ru.paymon.android.gateway.bitcoin.service.BlockchainService;
import ru.paymon.android.gateway.bitcoin.util.Crypto;
import ru.paymon.android.gateway.bitcoin.util.Io;
import ru.paymon.android.gateway.bitcoin.util.WalletUtils;
import ru.paymon.android.models.EthereumWallet;
import ru.paymon.android.models.PaymonWallet;
import ru.paymon.android.utils.Utils;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;


public class WalletApplication extends AbsWalletApplication {
    public static final int btcTxSize = (148 * 1) + (34 * 2) + 10;

    //    private final Executor getWalletExecutor = Executors.newSingleThreadExecutor();
    private final Object getWalletLock = new Object();
    //    private static final String BIP39_WORDLIST_FILENAME = "bip39-wordlist.txt";
//    private final WalletListener walletListener = new WalletListener();
    private Configuration config;
    private PackageInfo packageInfo;
    public static final String ACTION_WALLET_REFERENCE_CHANGED = WalletApplication.class.getPackage().getName() + ".wallet_reference_changed";
    private static final boolean IS_TEST = true;
    private static final String INFURA_LINK = IS_TEST ? "https://ropsten.infura.io/BAWTZQzsbBDZG6g9D0IP" : "https://mainnet.infura.io/BAWTZQzsbBDZG6g9D0IP";
    //    private Wallet bitcoinWallet;
    private EthereumWallet ethereumWallet;
    private PaymonWallet paymonWallet;
    //    private String bitcoinWalletPath;
    private String ethereumWalletPath;
    private String paymonWalletPath;
    private WalletAppKit kit;

    @Override
    public void onCreate() {
        new LinuxSecureRandom();

        Threading.throwOnLockCycles();
        org.bitcoinj.core.Context.enableStrictMode();
        org.bitcoinj.core.Context.propagate(Constants.CONTEXT);

        Executors.newSingleThreadExecutor().submit(() -> {
            test();
        });

        super.onCreate();

//        bitcoinWalletPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-btc-wallet.json";
        ethereumWalletPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        paymonWalletPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-pmnt-wallet.json";

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        walletFile = new File(bitcoinWalletPath);
        cleanupFiles();

        web3j = Web3jFactory.build(new HttpService(INFURA_LINK));
        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public void test() {
        kit = new WalletAppKit(Constants.NETWORK_PARAMETERS, new File(getCacheDir().getPath()), "walletappkit1-example");

        InputStream checkpoint = CheckpointManager.openStream(Constants.NETWORK_PARAMETERS);
        kit.setCheckpoints(checkpoint);
        kit.setAutoSave(true);
        kit.setBlockingStartup(false);

        Log.e("AAA", "before start");
        kit.startAsync();
        Log.e("AAA", "after start");
        kit.awaitRunning();
        Log.e("AAA", "after await");

        kit.wallet().addCoinsReceivedEventListener((Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) -> {
            Log.e("AAA", "-----> coins resceived: " + tx.getHashAsString());
            Log.e("AAA", "received: " + tx.getValue(wallet));
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED, BTC_CURRENCY_VALUE, newBalance.toPlainString());
        });

        kit.wallet().addCoinsSentEventListener((Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) -> {
            Log.e("AAA", "coins sent");
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED, BTC_CURRENCY_VALUE, newBalance.toPlainString());
        });

        kit.wallet().addKeyChainEventListener((List<ECKey> keys) -> {
            Log.e("AAA", "new key added");
        });

        kit.wallet().addScriptsChangeEventListener((Wallet wallet, List<Script> scripts, boolean isAddingScripts) -> {
            Log.e("AAA", "new script added");
        });

        kit.wallet().addTransactionConfidenceEventListener((Wallet wallet, Transaction tx) -> {
            Log.e("AAA", "-----> confidence changed: " + tx.getHashAsString());
            TransactionConfidence confidence = tx.getConfidence();
            Log.e("AAA", "new block depth: " + confidence.getDepthInBlocks());
        });

        // Make sure to properly shut down all the running services when you manually want to stop the kit. The WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our application is stopping.
        //Log.e("AAA", "shutting down again");
        //kit.stopAsync();
        //kit.awaitTerminated();
    }


//    public Wallet getBitcoinWallet() {
//        if (bitcoinWallet == null) {
//            getBitcoinWallet(User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD);
//        }
//        return bitcoinWallet;
//    }

    @Override
    public Wallet getBitcoinWallet() {
        if (kit.wallet().isEncrypted())
            kit.wallet().decrypt(User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD);
        return kit.wallet();
    }

    public EthereumWallet getEthereumWallet() {
        if (ethereumWallet == null) {
            getEthereumWallet(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD);
        }
        return ethereumWallet;
    }

    public PaymonWallet getPaymonWallet() {
        if (paymonWallet == null) {
            getPaymonWallet(User.CLIENT_MONEY_PAYMON_WALLET_PASSWORD);
        }
        return paymonWallet;
    }

//    @Override
//    protected Wallet getBitcoinWallet(final String password) {
//        final SettableFuture<Wallet> future = SettableFuture.create();
//        getBitcoinWalletAsync(wallet -> future.set(wallet));
//        try {
//            bitcoinWallet = future.get();
//            return bitcoinWallet;
//        } catch (final InterruptedException | ExecutionException x) {
//            throw new RuntimeException(x);
//        }
//    }

    @Override
    protected EthereumWallet getEthereumWallet(final String password) {
        try {
            walletCredentials = org.web3j.crypto.WalletUtils.loadCredentials(password, ethereumWalletPath);
            if (walletCredentials != null)
                ethereumWallet = new EthereumWallet(walletCredentials, password, "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ethereumWallet;
    }

    @Override
    protected PaymonWallet getPaymonWallet(final String password) {
        return null;
    }

//    @Override
//    public boolean createBitcoinWallet(final String password) {
//        final SettableFuture<Wallet> future = SettableFuture.create();
//        createBitcoinWalletAsync(password, wallet -> future.set(wallet));
//        try {
//            bitcoinWallet = future.get();
//            return bitcoinWallet != null;
//        } catch (final InterruptedException | ExecutionException x) {
//            return false;
//        }
//    }

    @Override
    public boolean createEthereumWallet(final String password) {
        final String FILE_FOLDER = getApplicationContext().getFilesDir().getAbsolutePath();
        deleteEthereumWallet();
        try {
            String fileName = org.web3j.crypto.WalletUtils.generateNewWalletFile(password, new File(FILE_FOLDER), false);
            if (fileName != null) {
                if (!Utils.copyFile(new File(FILE_FOLDER + "/" + fileName), new File(ethereumWalletPath)))
                    return false;
                new File(FILE_FOLDER + "/" + fileName).delete();
                ethereumWallet = getEthereumWallet(password);
                return ethereumWallet != null;
            }
            return false;
        } catch (CipherException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createPaymonWallet(final String password) {
        return false;
    }

    @Override
    public boolean backupBitcoinWallet(final String path) {
        try {
            final String BACKUP_FILE_PATH = path + "/" + "paymon-btc-wallet_backup_" + System.currentTimeMillis() + ".json";
            final File backupFile = new File(BACKUP_FILE_PATH);

            final BufferedReader cipherIn = new BufferedReader(new InputStreamReader(new FileInputStream(walletFile), Charsets.UTF_8));
            final StringBuilder cipherText = new StringBuilder();
            Io.copy(cipherIn, cipherText, Constants.BACKUP_MAX_CHARS);
            final String encryptedString = Crypto.encrypt(cipherText.toString(), User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD.toCharArray());
            FileOutputStream stream = new FileOutputStream(backupFile);
            stream.write(encryptedString.getBytes());
            stream.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

//    @Override
//    public Coin getBitcoinBalance() {
//        return bitcoinWallet.getBalance();
//    }

    @Override
    public Coin getBitcoinBalance() {
        return getBitcoinWallet().getBalance();
    }

    @Override
    public boolean backupEthereumWallet(final String path) {
        final String BACKUP_FILE_PATH = path + "/" + "paymon-eth-wallet_backup_" + System.currentTimeMillis() + ".json";
        File walletFile = new File(ethereumWalletPath);
        File backupFile = new File(BACKUP_FILE_PATH);

        if (!Utils.copyFile(walletFile, backupFile)) {
            android.widget.Toast.makeText(ApplicationLoader.applicationContext, "Скопировать файл кошелька не удалось", android.widget.Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public BigInteger getEthereumBalance() {
        BigInteger balance = null;
        try {
            balance = web3j.ethGetBalance(walletCredentials.getAddress(), DefaultBlockParameterName.fromString("latest")).send().getBalance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance;
    }

    @Override
    public boolean deleteEthereumWallet() {
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        return new File(FILE_PATH).delete();
    }

    public static String convertEthereumToFiat(final BigInteger ethAmount, final String fiatExRate) {
        return Convert.fromWei(new BigDecimal(ethAmount), Convert.Unit.GWEI).multiply(new BigDecimal(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    @Override
    public boolean backupPaymonWallet(final String path) {
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-pmnt-wallet.json";
        final String BACKUP_FILE_PATH = path + "/" + "paymon-pmnt-wallet_backup_" + System.currentTimeMillis() + ".json";
        File walletFile = new File(FILE_PATH);
        File walletInDownload = new File(BACKUP_FILE_PATH);

        if (!Utils.copyFile(walletFile, walletInDownload)) {
            android.widget.Toast.makeText(ApplicationLoader.applicationContext, "Скопировать файл кошелька не удалось", android.widget.Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public BigInteger getPaymonBalance() {
        return null;
    }

    @Override
    public boolean deletePaymonWallet() {
        return false;
    }


    public static String convertPaymonToFiat(final BigInteger pmntAmount, final String fiatExRate) {
        return Convert.fromWei(new BigDecimal(pmntAmount), Convert.Unit.GWEI).multiply(new BigDecimal(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    @Override
    public RestoreStatus restoreBitcoinWallet(final File file) {
        try {
            kit.wallet().loadFromFile(file);
            return RestoreStatus.DONE;
        } catch (Exception e) {
            return RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
        }
//        try {
//            final BufferedReader cipherIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
//            final StringBuilder cipherText = new StringBuilder();
//            Io.copy(cipherIn, cipherText, Constants.BACKUP_MAX_CHARS);
//            cipherIn.close();
//            final byte[] plainText = Crypto.decryptBytes(cipherText.toString(), password.toCharArray());
//            final InputStream is = new ByteArrayInputStream(plainText);
//            final Wallet wallet = WalletUtils.restoreWalletFromProtobufOrBase58(is, Constants.NETWORK_PARAMETERS, password);
//            replaceWallet(wallet);
//
//            return RestoreStatus.DONE;
//        } catch (final IOException x) {
//            x.printStackTrace();
//            return RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
//        }
    }

    @Override
    public boolean deleteBitcoinWallet() {
        return false;
    }

    public static String convertBitcoinToFiat(String btcAmount, String fiatExRate) {
        return new BigDecimal(btcAmount).multiply(new BigDecimal(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

//    @Override
//    public String getBitcoinPublicAddress() {
//        return bitcoinWallet.currentReceiveAddress().toBase58();
//    }
//
//    @Override
//    public String getBitcoinPrivateAddress() {
//        return bitcoinWallet.getActiveKeyChain().getWatchingKey().getPrivateKeyAsWiF(Constants.NETWORK_PARAMETERS);
//    }

    @Override
    public String getBitcoinPublicAddress() {
        return getBitcoinWallet().currentReceiveAddress().toBase58();
    }

    @Override
    public String getBitcoinPrivateAddress() {
        return getBitcoinWallet().getActiveKeyChain().getWatchingKey().getPrivateKeyAsWiF(Constants.NETWORK_PARAMETERS);
    }

    @Override
    public List<String> getAllBitcoinPublicAdresses() {
        List<ECKey> keys = getBitcoinWallet().getActiveKeyChain().getIssuedReceiveKeys();
        List<String> keysList = new ArrayList<>();
        for (ECKey key : keys) {
            keysList.add(new Address(Constants.NETWORK_PARAMETERS, key.getPubKeyHash()).toBase58());
        }
        return keysList;
    }

//    @Override
//    public List<String> getAllBitcoinPublicAdresses() {
//        List<ECKey> keys = bitcoinWallet.getActiveKeyChain().getIssuedReceiveKeys();
//        List<String> keysList = new ArrayList<>();
//        for (ECKey key : keys) {
//            keysList.add(new Address(Constants.NETWORK_PARAMETERS, key.getPubKeyHash()).toBase58());
//        }
//        return keysList;
//    }

    @Override
    public RestoreStatus restoreEthereumWallet(final File file, final String password) {
        final File walletFileForRestore = new File(ethereumWalletPath);
        deleteEthereumWallet();

        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(file);
            outputStream = new FileOutputStream(walletFileForRestore);
            IOUtils.copy(inputStream, outputStream);
            try {
                walletCredentials = null;
                ethereumWallet = getEthereumWallet(password);
                if (ethereumWallet != null)
                    return RestoreStatus.DONE;
                else
                    return RestoreStatus.NO_USER_ID;
            } catch (Exception e) {
                e.printStackTrace();
                return RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return RestoreStatus.ERROR_CREATE_FILE;
        }
    }

    @Override
    public RestoreStatus restorePaymonWallet(final File file, final String password) {
        return null;
    }

    @Override
    public EthSendTransaction sendRawEthereumTx(@NonNull String recipientAddress, @NonNull BigInteger ethAmount, @NonNull BigInteger gasPrise, @NonNull BigInteger gasLimit) {
        EthSendTransaction ethSendTransaction = null;
        try {
            BigInteger nonce = web3j.ethGetTransactionCount(walletCredentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrise, gasLimit, recipientAddress, ethAmount);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, walletCredentials);
            String hexValue = Numeric.toHexString(signedMessage);
            ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ethSendTransaction;
    }

    @Override
    public Transaction sendBitcoinTx(final String destinationAddress, final long satoshis, final long feePerB) {
        Coin value = Coin.valueOf(satoshis);
        Address to = Address.fromBase58(Constants.NETWORK_PARAMETERS, destinationAddress);

        Transaction transaction = new Transaction(Constants.NETWORK_PARAMETERS);
        transaction.addInput(getBitcoinWallet().getUnspents().get(0));// important to add proper input
        transaction.addOutput(value, to);

        SendRequest request1 = SendRequest.forTx(transaction);
        request1.feePerKb = Coin.valueOf(feePerB * 1000);

        Wallet.SendResult sendResult = null;
        try {
            sendResult = getBitcoinWallet().sendCoins(kit.peerGroup(), request1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendResult != null ? sendResult.tx : null;
    }

    public void replaceWallet(final Wallet newWallet) {
        newWallet.cleanup();
        BlockchainService.resetBlockchain(this);

        final Wallet oldWallet = getBitcoinWallet();
        synchronized (getWalletLock) {
            oldWallet.shutdownAutosaveAndWait(); // this will also prevent BlockchainService to save
            walletFiles = newWallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS, TimeUnit.MILLISECONDS, null);
        }
        config.maybeIncrementBestChainHeightEver(newWallet.getLastBlockSeenHeight());
        WalletUtils.autoBackupWallet(this, newWallet);

        final Intent broadcast = new Intent(ACTION_WALLET_REFERENCE_CHANGED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

//    @MainThread
//    private void createBitcoinWalletAsync(final String password, final OnWalletLoadedListener listener) {
//        getWalletExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
//                synchronized (getWalletLock) {
//                    initMnemonicCode();
//                    if (walletFiles == null)
//                        createWallet(password);
//                }
//                if (walletFiles != null) {
//                    bitcoinWallet = walletFiles.getWallet();
//                    if (bitcoinWallet.isEncrypted())
//                        bitcoinWallet.decrypt(password);
//                    bitcoinWallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletListener);
//                    bitcoinWallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletListener);
//                    bitcoinWallet.addReorganizeEventListener(Threading.SAME_THREAD, walletListener);
//                    bitcoinWallet.addChangeEventListener(Threading.SAME_THREAD, walletListener);
//                    listener.onWalletLoaded(bitcoinWallet);
//                }
//            }
//
//            @WorkerThread
//            private void createWallet(final String password) {
//                Wallet wallet = new Wallet(Constants.NETWORK_PARAMETERS);
//                if (!password.isEmpty())
//                    wallet.encrypt(password);
//                walletFiles = wallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS, TimeUnit.MILLISECONDS, null);
//                autosaveWalletNow();
//                WalletUtils.autoBackupWallet(WalletApplication.this, wallet);
//            }
//
//            private void initMnemonicCode() {
//                if (MnemonicCode.INSTANCE == null) {
//                    try {
//                        MnemonicCode.INSTANCE = new MnemonicCode(getAssets().open(BIP39_WORDLIST_FILENAME), null);
//                    } catch (final IOException x) {
//                        throw new Error(x);
//                    }
//                }
//            }
//        });
//    }
//
//
//    @MainThread
//    public void getBitcoinWalletAsync(final OnWalletLoadedListener listener) {
//        getWalletExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
//                synchronized (getWalletLock) {
//                    initMnemonicCode();
//                    if (walletFiles == null)
//                        loadWalletFromProtobuf();
//                }
//
//                if (walletFiles != null) {
//                    bitcoinWallet = walletFiles.getWallet();
//                    if (bitcoinWallet.isEncrypted())
//                        bitcoinWallet.decrypt(User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD);
//                    bitcoinWallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletListener);
//                    bitcoinWallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletListener);
//                    bitcoinWallet.addReorganizeEventListener(Threading.SAME_THREAD, walletListener);
//                    bitcoinWallet.addChangeEventListener(Threading.SAME_THREAD, walletListener);
//                    listener.onWalletLoaded(bitcoinWallet);
//                } else {
//                    listener.onWalletLoaded(null);
//                }
//            }
//
//            @WorkerThread
//            private void loadWalletFromProtobuf() {
//                if (walletFile.exists()) {
//                    try (final FileInputStream walletStream = new FileInputStream(walletFile)) {
//                        bitcoinWallet = new WalletProtobufSerializer().readWallet(walletStream);
//
//                        if (!bitcoinWallet.getParams().equals(Constants.NETWORK_PARAMETERS))
//                            throw new UnreadableWalletException("bad wallet network parameters: " + bitcoinWallet.getParams().getId());
//
//                    } catch (final IOException | UnreadableWalletException x) {
//                        bitcoinWallet = WalletUtils.restoreWalletFromAutoBackup(WalletApplication.this);
//                        if (bitcoinWallet != null)
//                            new Toast(WalletApplication.this).postLongToast(R.string.toast_wallet_reset);
//                    }
//                    if (!bitcoinWallet.isConsistent()) {
//                        bitcoinWallet = WalletUtils.restoreWalletFromAutoBackup(WalletApplication.this);
//                        if (bitcoinWallet != null)
//                            new Toast(WalletApplication.this).postLongToast(R.string.toast_wallet_reset);
//                    }
//
//                    if (!bitcoinWallet.getParams().equals(Constants.NETWORK_PARAMETERS))
//                        throw new Error("bad wallet network parameters: " + bitcoinWallet.getParams().getId());
//
//                    bitcoinWallet.cleanup();
//                    walletFiles = bitcoinWallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS, TimeUnit.MILLISECONDS, null);
//                }
//            }
//
//            private void initMnemonicCode() {
//                if (MnemonicCode.INSTANCE == null) {
//                    try {
//                        MnemonicCode.INSTANCE = new MnemonicCode(getAssets().open(BIP39_WORDLIST_FILENAME), null);
//                    } catch (final IOException x) {
//                        throw new Error(x);
//                    }
//                }
//            }
//        });
//    }

    public void autosaveWalletNow() {
        synchronized (getWalletLock) {
            if (walletFiles != null) {
                try {
                    walletFiles.saveNow();
                } catch (final IOException x) {
                }
            }
        }
    }

    private void cleanupFiles() {
        for (final String filename : fileList()) {
            if (filename.startsWith(Constants.Files.WALLET_KEY_BACKUP_BASE58)
                    || filename.startsWith(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF + '.')
                    || filename.endsWith(".tmp")) {
                final File file = new File(getFilesDir(), filename);
                file.delete();
            }
        }
    }

//    private class WalletListener implements WalletCoinsReceivedEventListener, WalletCoinsSentEventListener, WalletReorganizeEventListener, WalletChangeEventListener {
//        @Override
//        public void onCoinsReceived(final Wallet wallet, final Transaction tx, final Coin prevBalance, final Coin newBalance) {
//            Log.e("AAA", prevBalance + "  received QQ " + newBalance);
//            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED, BTC_CURRENCY_VALUE, newBalance.toString());
//        }
//
//        @Override
//        public void onCoinsSent(final Wallet wallet, final Transaction tx, final Coin prevBalance, final Coin newBalance) {
//            Log.e("AAA", prevBalance + " QQ " + newBalance);
//            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED, BTC_CURRENCY_VALUE, newBalance.toString());
//        }
//
//        @Override
//        public void onReorganize(final Wallet wallet) {
//
//        }
//
//        @Override
//        public void onWalletChanged(final Wallet wallet) {
//
//        }
//    }
//
//    public interface OnWalletLoadedListener {
//        void onWalletLoaded(Wallet wallet);
//    }

    public final String applicationPackageFlavor() {
        final String packageName = getPackageName();
        final int index = packageName.lastIndexOf('_');

        if (index != -1)
            return packageName.substring(index + 1);
        else
            return null;
    }

    public synchronized Configuration getConfiguration() {
        if (config == null)
            config = new Configuration(PreferenceManager.getDefaultSharedPreferences(this), getResources());
        return config;
    }

    public synchronized PackageInfo packageInfo() {
        // replace by BuildConfig.VERSION_* as soon as it's possible
        if (packageInfo == null) {
            try {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (final PackageManager.NameNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
        return packageInfo;
    }

    public int maxConnectedPeers() {
        return activityManager.isLowRamDevice() ? 4 : 6;
    }

    public static String httpUserAgent(final String versionName) {
        final VersionMessage versionMessage = new VersionMessage(Constants.NETWORK_PARAMETERS, 0);
        versionMessage.appendToSubVer(Constants.USER_AGENT, versionName, null);
        return versionMessage.subVer;
    }
}
