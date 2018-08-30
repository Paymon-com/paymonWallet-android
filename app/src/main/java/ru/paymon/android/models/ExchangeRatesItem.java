package ru.paymon.android.models;

public class ExchangeRatesItem {
    public String cryptoCurrency;
    public String fiatCurrency;
    public float value;

    public ExchangeRatesItem(String cryptoCurrency, String fiatCurrency, float value) {
        this.cryptoCurrency = cryptoCurrency;
        this.fiatCurrency = fiatCurrency;
        this.value = value;
    }
}
