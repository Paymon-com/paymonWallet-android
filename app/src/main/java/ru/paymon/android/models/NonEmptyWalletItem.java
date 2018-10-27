package ru.paymon.android.models;

import android.util.Log;

import java.math.BigInteger;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.WalletApplication;

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class NonEmptyWalletItem extends WalletItem implements NotificationManager.IListener {
    public String cryptoBalance;
    public String fiatCurrency;
    public String fiatBalance;

    public NonEmptyWalletItem(final String cryptoCurrency, final String cryptoBalance, final String fiatCurrency, final String fiatBalance) {
        super(cryptoCurrency);
        this.cryptoBalance = cryptoBalance;
        this.fiatCurrency = fiatCurrency;
        this.fiatBalance = fiatBalance;
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.MONEY_FIAT_CURRENCY_CHANGED);
    }

    public NonEmptyWalletItem(final String cryptoCurrency, final String cryptoBalance) {
        super(cryptoCurrency);
        this.cryptoBalance = cryptoBalance;
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.MONEY_FIAT_CURRENCY_CHANGED);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.MONEY_FIAT_CURRENCY_CHANGED);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.MONEY_FIAT_CURRENCY_CHANGED) {
            fiatCurrency = (String) args[0];
            ExchangeRate exchangeRate = ApplicationLoader.db.exchangeRatesDao().getExchangeRatesByFiatAndCryptoCurrecy(fiatCurrency, cryptoCurrency);
            if(exchangeRate == null) return;
            switch (cryptoCurrency) {
                case BTC_CURRENCY_VALUE:
                    fiatBalance = WalletApplication.convertBitcoinToFiat(cryptoBalance, exchangeRate.value);
                    break;
                case ETH_CURRENCY_VALUE:
                    fiatBalance = WalletApplication.convertEthereumToFiat(cryptoBalance, exchangeRate.value);
                    break;
                case PMNT_CURRENCY_VALUE:
                    fiatBalance = WalletApplication.convertPaymonToFiat(cryptoBalance, exchangeRate.value);
                    break;
            }
        } else if (event == NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED) {
            String argsCryptoCurrency = (String) args[0];
            if (argsCryptoCurrency.equals(cryptoCurrency))
                cryptoBalance = (String) args[1];
        }
    }
}
