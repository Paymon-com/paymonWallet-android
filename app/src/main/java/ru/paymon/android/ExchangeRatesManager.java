package ru.paymon.android;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.net.RPC;
import ru.paymon.android.room.AppDatabase;

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;

public class ExchangeRatesManager {
    private static volatile ExchangeRatesManager Instance = null;

    public static ExchangeRatesManager getInstance() {
        ExchangeRatesManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (ExchangeRatesManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ExchangeRatesManager();
                }
            }
        }
        return localInstance;
    }

    public void putExchangeRate(ExchangeRate exchangeRate) {
//        Executors.newSingleThreadExecutor().submit(() -> );
    }



    public List<ExchangeRate> getExchangeRatesByCryptoCurrency(String cryptoCurrency){
        Callable<List<ExchangeRate>> callable = new Callable<List<ExchangeRate>>() {
            @Override
            public List<ExchangeRate> call() {
                return AppDatabase.getDatabase().exchangeRatesDao().getExchangeRatesByCryptoCurrecy(cryptoCurrency);
            }
        };

        Future<List<ExchangeRate>> future = Executors.newSingleThreadExecutor().submit(callable);

        List<ExchangeRate> exchangeRates = null;
        try {
            exchangeRates = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exchangeRates;
    }

    public ExchangeRate getExchangeRatesByFiatAndCryptoCurrecy(String fiatCurrency, String cryptoCurrency){
        Callable<ExchangeRate> callable = new Callable<ExchangeRate>() {
            @Override
            public ExchangeRate call() {
                return AppDatabase.getDatabase().exchangeRatesDao().getExchangeRatesByFiatAndCryptoCurrecy(fiatCurrency,cryptoCurrency);
            }
        };

        Future<ExchangeRate> future = Executors.newSingleThreadExecutor().submit(callable);

        ExchangeRate exchangeRates = null;
        try {
            exchangeRates = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exchangeRates;
    }
}
