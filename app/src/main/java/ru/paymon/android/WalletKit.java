package ru.paymon.android;

import android.app.ActivityManager;
import android.util.Log;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import ru.paymon.android.utils.Constants;
import ru.paymon.android.utils.DispatchQueue;

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;

public class WalletKit extends WalletAppKit {
    private static WalletKit instance;


    public static WalletKit getInstance() {
        if (instance == null)
            instance = new WalletKit(Constants.NETWORK_PARAMETERS, new File(ApplicationLoader.applicationContext.getCacheDir().getPath()), "paymon-btc-wallet");
        return instance;
    }

    public static WalletKit newInstance() {
        instance = new WalletKit(Constants.NETWORK_PARAMETERS, new File(ApplicationLoader.applicationContext.getCacheDir().getPath()), "paymon-btc-wallet");
        return instance;
    }

    public WalletKit(NetworkParameters params, File directory, String filePrefix) {
        super(params, directory, filePrefix);
        InputStream checkpoint = CheckpointManager.openStream(Constants.NETWORK_PARAMETERS);
        setCheckpoints(checkpoint);
        setAutoSave(true);
        setBlockingStartup(false);

        setDownloadListener(new DownloadProgressTracker() {
            @Override
            public void onChainDownloadStarted(Peer peer, int blocksLeft) {
                Log.e("AAA", "chain download started");
                super.onChainDownloadStarted(peer, blocksLeft);
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_CHAIN_STARTED);
            }

            @Override
            public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                Log.e("AAA", "blocks downloaded");
                super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
            }

            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                Log.e("AAA", "progress " + pct);
                super.progress(pct, blocksSoFar, date);
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_PROGRESS, pct);
            }

            @Override
            protected void startDownload(int blocks) {
                Log.e("AAA", "download started");
                super.startDownload(blocks);
            }

            @Override
            protected void doneDownload() {
                Log.e("AAA", "download done");
                super.doneDownload();
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_FINISHED);
            }
        });
    }

    public WalletKit(Context context, File directory, String filePrefix) {
        super(context, directory, filePrefix);
        InputStream checkpoint = CheckpointManager.openStream(Constants.NETWORK_PARAMETERS);
        setCheckpoints(checkpoint);
        setAutoSave(true);
        setBlockingStartup(false);

        setDownloadListener(new DownloadProgressTracker() {
            @Override
            public void onChainDownloadStarted(Peer peer, int blocksLeft) {
                Log.e("AAA", "chain download started");
                super.onChainDownloadStarted(peer, blocksLeft);
            }

            @Override
            public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                Log.e("AAA", "blocks downloaded");
                super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
            }

            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                Log.e("AAA", "progress " + pct);
                super.progress(pct, blocksSoFar, date);
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_PROGRESS, pct);
            }

            @Override
            protected void startDownload(int blocks) {
                Log.e("AAA", "download started");
                super.startDownload(blocks);
            }

            @Override
            protected void doneDownload() {
                Log.e("AAA", "download done");
                super.doneDownload();
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_FINISHED);
            }
        });
    }

    public Wallet restoreWallet(final Wallet wallet) throws Exception {
        shutDown();
        wallet.cleanup();
        File chainFile = new File(directory, filePrefix + ".spvchain");
        chainFile.delete();
        vWalletFile = new File(directory, filePrefix + ".wallet");
        wallet.saveToFile(vWalletFile);
        startUp();
        return vWallet;
    }

    public boolean deleteWallet() throws Exception {
        shutDown();
        File chainFile = new File(directory, filePrefix + ".spvchain");
        chainFile.delete();
        vWallet = null;
        return vWalletFile.delete();
    }

    public void startBitcoinKit() {
        if (isRunning()) return;

        startAsync();
        awaitRunning();

        wallet().addCoinsReceivedEventListener((Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) -> {
            Log.e("AAA", "-----> coins received: " + tx.getHashAsString());
            Log.e("AAA", "received: " + tx.getValue(wallet));
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED, BTC_CURRENCY_VALUE, wallet().getBalance().toPlainString());
        });

        wallet().addCoinsSentEventListener((Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) -> {
            Log.e("AAA", "coins sent");
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED, BTC_CURRENCY_VALUE, wallet().getBalance().toPlainString());
        });

        wallet().addKeyChainEventListener((List<ECKey> keys) -> {
            Log.e("AAA", "new key added");
        });

        wallet().addScriptsChangeEventListener((Wallet wallet, List<Script> scripts, boolean isAddingScripts) -> {
            Log.e("AAA", "new script added");
        });

        wallet().addTransactionConfidenceEventListener((Wallet wallet, Transaction tx) -> {
            Log.e("AAA", "-----> confidence changed: " + tx.getHashAsString());
            TransactionConfidence confidence = tx.getConfidence();
            Log.e("AAA", "new block depth: " + confidence.getDepthInBlocks());
        });
    }

}
