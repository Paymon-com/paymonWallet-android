package ru.paymon.android.gateway.bitcoin.data;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.BalanceType;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;

import ru.paymon.android.WalletApplication;
import ru.paymon.android.gateway.bitcoin.Configuration;
import ru.paymon.android.gateway.bitcoin.Constants;

public final class WalletBalanceLiveData extends AbstractWalletLiveData<Coin>
        implements OnSharedPreferenceChangeListener {
    private final BalanceType balanceType;
    private final Configuration config;

    public WalletBalanceLiveData(final WalletApplication application, final BalanceType balanceType) {
        super(application);
        this.balanceType = balanceType;
        this.config = application.getConfiguration();
    }

    public WalletBalanceLiveData(final WalletApplication application) {
        this(application, BalanceType.ESTIMATED);
    }

    @Override
    protected void onWalletActive(final Wallet wallet) {
        addWalletListener(wallet);
        config.registerOnSharedPreferenceChangeListener(this);
        load();
    }

    @Override
    protected void onWalletInactive(final Wallet wallet) {
        config.unregisterOnSharedPreferenceChangeListener(this);
        removeWalletListener(wallet);
    }

    private void addWalletListener(final Wallet wallet) {
        wallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addReorganizeEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addChangeEventListener(Threading.SAME_THREAD, walletListener);
    }

    private void removeWalletListener(final Wallet wallet) {
        wallet.removeChangeEventListener(walletListener);
        wallet.removeReorganizeEventListener(walletListener);
        wallet.removeCoinsSentEventListener(walletListener);
        wallet.removeCoinsReceivedEventListener(walletListener);
    }

    @Override
    protected void load() {
        final Wallet wallet = getWallet();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
                postValue(wallet.getBalance(balanceType));
            }
        });
    }

    private final WalletListener walletListener = new WalletListener();

    private class WalletListener implements WalletCoinsReceivedEventListener, WalletCoinsSentEventListener,
            WalletReorganizeEventListener, WalletChangeEventListener {
        @Override
        public void onCoinsReceived(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                                    final Coin newBalance) {
            triggerLoad();
        }

        @Override
        public void onCoinsSent(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                                final Coin newBalance) {
            triggerLoad();
        }

        @Override
        public void onReorganize(final Wallet wallet) {
            triggerLoad();
        }

        @Override
        public void onWalletChanged(final Wallet wallet) {
            triggerLoad();
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (Configuration.PREFS_KEY_BTC_PRECISION.equals(key))
            load();
    }
}
