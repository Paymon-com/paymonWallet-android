package ru.paymon.android.models;

public class NonEmptyWalletItem extends WalletItem {
    public String cryptoBalance;
    public String fiatCurrency;
    public String fiatBalance;

    public NonEmptyWalletItem(final String cryptoCurrency, final String cryptoBalance, final String fiatBalance, final String fiatCurrency) {
        super(cryptoCurrency);
        this.cryptoBalance = cryptoBalance;
        this.fiatCurrency = fiatCurrency;
        this.fiatBalance = fiatBalance;
    }

    public NonEmptyWalletItem(final String cryptoCurrency, final String cryptoBalance) {
        super(cryptoCurrency);
        this.cryptoBalance = cryptoBalance;
    }
}
