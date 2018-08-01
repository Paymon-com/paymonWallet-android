package ru.paymon.android.models;

public class Wallet {
    public String cryptoCurrency;
    public String fiatCurrency;
    public String cryptoBalance;
    public String fiatBalance;
    public boolean isEmpty;

    public Wallet(String cryptoCurrency, String fiatCurrency, String cryptoBalance, String fiatBalance, boolean isEmpty) {
        this.cryptoCurrency = cryptoCurrency;
        this.cryptoBalance = cryptoBalance;
        this.fiatBalance = fiatBalance;
        this.fiatCurrency = fiatCurrency;
        this.isEmpty = isEmpty;
    }
}
