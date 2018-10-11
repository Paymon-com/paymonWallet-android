package ru.paymon.android.gateway.bitcoin.data;

import org.bitcoinj.wallet.Wallet;

import ru.paymon.android.WalletApplication;


public class WalletLiveData extends AbstractWalletLiveData<Wallet> {
    public WalletLiveData(final WalletApplication application) {
        super(application, 0);
    }

    @Override
    protected void onWalletActive(final Wallet wallet) {
        postValue(wallet);
    }
}
