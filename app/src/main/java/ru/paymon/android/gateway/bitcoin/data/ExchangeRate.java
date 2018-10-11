package ru.paymon.android.gateway.bitcoin.data;
import static android.support.v4.util.Preconditions.checkNotNull;

public class ExchangeRate {
    public ExchangeRate(final org.bitcoinj.utils.ExchangeRate rate, final String source) {
        checkNotNull(rate.fiat.currencyCode);

        this.rate = rate;
        this.source = source;
    }

    public final org.bitcoinj.utils.ExchangeRate rate;
    public final String source;

    public String getCurrencyCode() {
        return rate.fiat.currencyCode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + rate.fiat + ']';
    }
}