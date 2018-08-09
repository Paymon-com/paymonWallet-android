package ru.paymon.android.models;

public class ExchangeRatesItem {
    public String cryptoCurrency;
    public String fiatCurrency;
    public String value;

    public ExchangeRatesItem(String cryptoCurrency, String fiatCurrency, String value) {
        this.cryptoCurrency = cryptoCurrency;
        this.fiatCurrency = fiatCurrency;
        this.value = value;
    }
}
