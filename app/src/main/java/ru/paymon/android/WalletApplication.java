package ru.paymon.android;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.toolbox.Volley;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.SettableFuture;

import org.apache.commons.io.IOUtils;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.VersionMessage;
import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletProtobufSerializer;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.paymon.android.gateway.bitcoin.Configuration;
import ru.paymon.android.gateway.bitcoin.Constants;
import ru.paymon.android.gateway.bitcoin.util.Toast;
import ru.paymon.android.gateway.bitcoin.util.WalletUtils;
import ru.paymon.android.gateway.ethereum.EthereumWallet;
import ru.paymon.android.utils.Utils;


public class WalletApplication extends AbsWalletApplication {
    private final Executor getWalletExecutor = Executors.newSingleThreadExecutor();
    private final Object getWalletLock = new Object();
    private static final String BIP39_WORDLIST_FILENAME = "bip39-wordlist.txt";
    private final WalletListener walletListener = new WalletListener();
    private Configuration config;
    private PackageInfo packageInfo;
    public static final String ACTION_WALLET_REFERENCE_CHANGED = WalletApplication.class.getPackage().getName() + ".wallet_reference_changed";
    private static final boolean IS_TEST = true;
    private static final String INFURA_LINK = IS_TEST ? "https://ropsten.infura.io/BAWTZQzsbBDZG6g9D0IP" : "https://mainnet.infura.io/BAWTZQzsbBDZG6g9D0IP";


    @Override
    public void onCreate() {
        new LinuxSecureRandom();

        Threading.throwOnLockCycles();
        org.bitcoinj.core.Context.enableStrictMode();
        org.bitcoinj.core.Context.propagate(Constants.CONTEXT);

        super.onCreate();

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        walletFile = getFileStreamPath(Constants.Files.WALLET_FILENAME_PROTOBUF);
        cleanupFiles();

        web3j = Web3jFactory.build(new HttpService(INFURA_LINK));
        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public Wallet getBitcoinWallet() {
        final Stopwatch watch = Stopwatch.createStarted();
        final SettableFuture<Wallet> future = SettableFuture.create();
        getBitcoinWalletAsync(wallet -> future.set(wallet));
        try {
            return future.get();
        } catch (final InterruptedException | ExecutionException x) {
            throw new RuntimeException(x);
        } finally {
            watch.stop();
        }
    }

    @Override
    public EthereumWallet getEthereumWallet(final String password) {
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        EthereumWallet wallet = null;
        try {
            walletCredentials = org.web3j.crypto.WalletUtils.loadCredentials(password, FILE_PATH);
            if (walletCredentials != null)
                wallet = new EthereumWallet(walletCredentials, password, "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wallet;
    }

    @Override
    public Wallet getPaymonWallet() {
        return null;
    }

    @Override
    public boolean createBitcoinWallet() {
        final Stopwatch watch = Stopwatch.createStarted();
        final SettableFuture<Wallet> future = SettableFuture.create();
        createBitcoinWalletAsync(wallet -> future.set(wallet));
        try {
            future.get();
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BITCOIN_WALLET_CREATED);
            return true;
        } catch (final InterruptedException | ExecutionException x) {
            return false;
        } finally {
            watch.stop();
        }
    }

    @Override
    public boolean createEthereumWallet(final String password) {
        final String FILE_FOLDER = getApplicationContext().getFilesDir().getAbsolutePath();
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        deleteEthereumWallet();
        try {
            String fileName = org.web3j.crypto.WalletUtils.generateNewWalletFile(password, new File(FILE_FOLDER), false);
            if (fileName != null) {
                if (!Utils.copyFile(new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/" + fileName), new File(FILE_PATH)))
                    return false;
                EthereumWallet wallet = getEthereumWallet(password);
                return wallet != null;
            }
            return false;
        } catch (CipherException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Wallet createPaymonWallet() {
        return null;
    }

    @Override
    public Wallet backupBitcoinWallet() {
        return null;
    }

    @Override
    public boolean backupEthereumWallet(final String path) {
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        final String BACKUP_FILE_PATH = path + "/" + "paymon-eth-wallet_backup_" + System.currentTimeMillis() + ".json";
        File walletFile = new File(FILE_PATH);
        File walletInDownload = new File(BACKUP_FILE_PATH);

        if (!Utils.copyFile(walletFile, walletInDownload)) {
            android.widget.Toast.makeText(ApplicationLoader.applicationContext, "Скопировать файл кошелька не удалось", android.widget.Toast.LENGTH_LONG).show();
            return false;
        }

        if (ContextCompat.checkSelfPermission(ApplicationLoader.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {//TODO:запрос прав
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("application/json");

            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(ApplicationLoader.applicationContext, BuildConfig.APPLICATION_ID + ".provider", walletInDownload));

            try {
                ApplicationLoader.applicationContext.startActivity(Intent.createChooser(intent, ""));
                return true;
            } catch (Exception e) {
                android.widget.Toast.makeText(ApplicationLoader.applicationContext, R.string.export_keys_dialog_mail_intent_failed, android.widget.Toast.LENGTH_LONG).show();
                return false;
            }

        } else {
            android.widget.Toast.makeText(ApplicationLoader.applicationContext, R.string.right_file_system, android.widget.Toast.LENGTH_SHORT).show();
            return false;
        }
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

    @Override
    public String convertEthereumToFiat(final String ethAmount, final float fiatExRate) {
        return new BigDecimal(ethAmount).multiply(new BigDecimal(fiatExRate)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    @Override
    public Wallet backupPaymonWallet() {
        return null;
    }

    @Override
    public boolean deletePaymonWallet() {
        return false;
    }

    @Override
    public Wallet restoreBitcoinWallet() {
        return null;
    }

    @Override
    public boolean deleteBitcoinWallet() {
        return false;
    }

    @Override
    public RestoreStatus restoreEthereumWallet(final File file, final String password) {
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        final File walletFileForRestore = new File(FILE_PATH);
        deleteEthereumWallet();

        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(file);
            outputStream = new FileOutputStream(walletFileForRestore);
            IOUtils.copy(inputStream, outputStream);
            try {
                walletCredentials = null;
                EthereumWallet wallet = getEthereumWallet(password);
                if (wallet != null)
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
    public Wallet restorePaymonWallet() {
        return null;
    }

    @Override
    protected void setBitcoinWalletListeners() {

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


    @MainThread
    private void createBitcoinWalletAsync(final OnWalletLoadedListener listener) {
        getWalletExecutor.execute(new Runnable() {
            @Override
            public void run() {
                org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
                synchronized (getWalletLock) {
                    initMnemonicCode();
                    if (walletFiles == null)
                        createWallet();
                }
                if (walletFiles != null) {
                    Wallet wallet = walletFiles.getWallet();
                    if (wallet != null) {
                        wallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletListener);
                        wallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletListener);
                        wallet.addReorganizeEventListener(Threading.SAME_THREAD, walletListener);
                        wallet.addChangeEventListener(Threading.SAME_THREAD, walletListener);
                        listener.onWalletLoaded(wallet);
                    }
                }
            }

            @WorkerThread
            private void createWallet() {
                final Stopwatch watch = Stopwatch.createStarted();
                Wallet wallet = new Wallet(Constants.NETWORK_PARAMETERS);
                walletFiles = wallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS, TimeUnit.MILLISECONDS, null);
                autosaveWalletNow();
                WalletUtils.autoBackupWallet(WalletApplication.this, wallet);
                watch.stop();
                Log.e("AAA", "wallet created ");
            }

            private void initMnemonicCode() {
                if (MnemonicCode.INSTANCE == null) {
                    try {
                        final Stopwatch watch = Stopwatch.createStarted();
                        MnemonicCode.INSTANCE = new MnemonicCode(getAssets().open(BIP39_WORDLIST_FILENAME), null);
                        watch.stop();
                    } catch (final IOException x) {
                        throw new Error(x);
                    }
                }
            }
        });
    }


    @MainThread
    public void getBitcoinWalletAsync(final OnWalletLoadedListener listener) {
        getWalletExecutor.execute(new Runnable() {
            @Override
            public void run() {
                org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
                synchronized (getWalletLock) {
                    initMnemonicCode();
                    if (walletFiles == null)
                        loadWalletFromProtobuf();
                }

                if (walletFiles != null) {
                    Wallet wallet = walletFiles.getWallet();
                    if (wallet != null) {
                        wallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletListener);
                        wallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletListener);
                        wallet.addReorganizeEventListener(Threading.SAME_THREAD, walletListener);
                        wallet.addChangeEventListener(Threading.SAME_THREAD, walletListener);
                        listener.onWalletLoaded(wallet);
                    } else {
                        listener.onWalletLoaded(null);
                    }
                } else {
                    listener.onWalletLoaded(null);
                }
            }

            @WorkerThread
            private void loadWalletFromProtobuf() {
                Wallet wallet;
                if (walletFile.exists()) {
                    try (final FileInputStream walletStream = new FileInputStream(walletFile)) {
                        final Stopwatch watch = Stopwatch.createStarted();
                        wallet = new WalletProtobufSerializer().readWallet(walletStream);
                        watch.stop();

                        if (!wallet.getParams().equals(Constants.NETWORK_PARAMETERS))
                            throw new UnreadableWalletException("bad wallet network parameters: " + wallet.getParams().getId());

                        Log.e("AAA", "wallet from " + walletFile);
                    } catch (final IOException | UnreadableWalletException x) {
                        wallet = WalletUtils.restoreWalletFromAutoBackup(WalletApplication.this);
                        if (wallet != null)
                            new Toast(WalletApplication.this).postLongToast(R.string.toast_wallet_reset);
                    }
                    if (!wallet.isConsistent()) {
                        wallet = WalletUtils.restoreWalletFromAutoBackup(WalletApplication.this);
                        if (wallet != null)
                            new Toast(WalletApplication.this).postLongToast(R.string.toast_wallet_reset);
                    }

                    if (!wallet.getParams().equals(Constants.NETWORK_PARAMETERS))
                        throw new Error("bad wallet network parameters: " + wallet.getParams().getId());

                    wallet.cleanup();
                    walletFiles = wallet.autosaveToFile(walletFile, Constants.Files.WALLET_AUTOSAVE_DELAY_MS, TimeUnit.MILLISECONDS, null);
                } else {

                }
            }

            private void initMnemonicCode() {
                if (MnemonicCode.INSTANCE == null) {
                    try {
                        final Stopwatch watch = Stopwatch.createStarted();
                        MnemonicCode.INSTANCE = new MnemonicCode(getAssets().open(BIP39_WORDLIST_FILENAME), null);
                        watch.stop();
                    } catch (final IOException x) {
                        throw new Error(x);
                    }
                }
            }
        });
    }

    public void autosaveWalletNow() {
        final Stopwatch watch = Stopwatch.createStarted();
        synchronized (getWalletLock) {
            if (walletFiles != null) {
                watch.stop();
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

    private class WalletListener implements WalletCoinsReceivedEventListener, WalletCoinsSentEventListener, WalletReorganizeEventListener, WalletChangeEventListener {
        @Override
        public void onCoinsReceived(final Wallet wallet, final Transaction tx, final Coin prevBalance, final Coin newBalance) {

        }

        @Override
        public void onCoinsSent(final Wallet wallet, final Transaction tx, final Coin prevBalance, final Coin newBalance) {

        }

        @Override
        public void onReorganize(final Wallet wallet) {

        }

        @Override
        public void onWalletChanged(final Wallet wallet) {

        }
    }

    public interface OnWalletLoadedListener {
        void onWalletLoaded(Wallet wallet);
    }

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
