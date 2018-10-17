package ru.paymon.android.gateway.exchangerates;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ExchangeRate {
    @PrimaryKey
    public int id;
    public String fiatCurrency;
    public String cryptoCurrency;
    public String value;

    public ExchangeRate(final int id, final String fiatCurrency, final String cryptoCurrency, final String value) {
        this.id = id;
        this.fiatCurrency = fiatCurrency;
        this.cryptoCurrency = cryptoCurrency;
        this.value = value;
    }
}
