package ru.paymon.android.gateway.bitcoin.data;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.bitcoinj.wallet.Wallet;

import ru.paymon.android.WalletApplication;

/**
 * @author Andreas Schildbach
 */
public abstract class AbstractWalletLiveData<T> extends ThrottelingLiveData<T> {
    private final WalletApplication application;
    private final LocalBroadcastManager broadcastManager;
    private final Handler handler = new Handler();
    private Wallet wallet;

    public AbstractWalletLiveData(final WalletApplication application) {
        super();
        this.application = application;
        this.broadcastManager = LocalBroadcastManager.getInstance(application);
    }

    public AbstractWalletLiveData(final WalletApplication application, final long throttleMs) {
        super(throttleMs);
        this.application = application;
        this.broadcastManager = LocalBroadcastManager.getInstance(application);
    }

    @Override
    protected final void onActive() {
        broadcastManager.registerReceiver(walletReferenceChangeReceiver,
                new IntentFilter(WalletApplication.ACTION_WALLET_REFERENCE_CHANGED));
        loadWallet();
    }

    @Override
    protected final void onInactive() {
        // TODO cancel async loading
        if (wallet != null)
            onWalletInactive(wallet);
        broadcastManager.unregisterReceiver(walletReferenceChangeReceiver);
    }

    private void loadWallet() {
        application.getWalletAsync(onWalletLoadedListener);
    }

    protected Wallet getWallet() {
        return wallet;
    }

    private final WalletApplication.OnWalletLoadedListener onWalletLoadedListener = new WalletApplication.OnWalletLoadedListener() {
        @Override
        public void onWalletLoaded(final Wallet wallet) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AbstractWalletLiveData.this.wallet = wallet;
                    onWalletActive(wallet);
                }
            });
        }
    };

    private final BroadcastReceiver walletReferenceChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (wallet != null)
                onWalletInactive(wallet);
            loadWallet();
        }
    };

    protected abstract void onWalletActive(Wallet wallet);

    protected void onWalletInactive(final Wallet wallet) {
        // do nothing by default
    };
}
