package ru.paymon.android.models;

public class WalletItem {
    public String cryptoCurrency;
    public String fiatCurrency;
    public String cryptoBalance;
    public String fiatBalance;
    public boolean isEmpty;

    public WalletItem(String cryptoCurrency, String fiatCurrency, String cryptoBalance, String fiatBalance, boolean isEmpty) {
        this.cryptoCurrency = cryptoCurrency;
        this.cryptoBalance = cryptoBalance;
        this.fiatBalance = fiatBalance;
        this.fiatCurrency = fiatCurrency;
        this.isEmpty = isEmpty;
    }
}
