package ru.paymon.android.models;

public class NonEmptyWalletItem extends WalletItem {
    public String cryptoBalance;
    public String publicAddress;

    public NonEmptyWalletItem(String cryptoCurrency, String cryptoBalance, String publicAddress) {
        super(cryptoCurrency);
        this.cryptoBalance = cryptoBalance;
        this.publicAddress = publicAddress;
    }
}
