package ru.paymon.android.utils;

import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.DBHelper;
import ru.paymon.android.adapters.ExchangeRatesAdapter;
import ru.paymon.android.models.ExchangeRatesItem;

public class ExchangeRates {
    private static ExchangeRates instance;
    public HashSet<String> fiatCurrencies = new HashSet<>();

    public static ExchangeRates getInstance(){
        if(instance == null)
            instance = new ExchangeRates();
        return instance;
    }

    private ExchangeRates(){
        fiatCurrencies.add("USD");
        fiatCurrencies.add("EUR");
        fiatCurrencies.add(Currency.getInstance(Locale.getDefault()).getCurrencyCode().toUpperCase());
    }

    public HashMap<String, HashMap<String, ExchangeRatesItem>> parseExchangeRates(){
        final HashMap<String, HashMap<String, ExchangeRatesItem>> exchangeRates = new HashMap<>();
        final String[] cryptoCurrencies = new String[]{"BTC", "ETH", "PMNT"};
        final String link = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=" + Arrays.toString(cryptoCurrencies).replace("[", "").replace("]", "").replace(" ", "") + "&tsyms=" + fiatCurrencies.toString().replace("[", "").replace("]", "").replace(" ", "");

        try {
            final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) ((new URL(link.toString()).openConnection()));
            httpsURLConnection.setConnectTimeout(20000);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.connect();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            final StringBuilder stringBuilder = new StringBuilder();
            String response;
            while ((response = bufferedReader.readLine()) != null) {
                stringBuilder.append(response);
            }
            bufferedReader.close();

            final JSONObject jsonObject = new JSONObject(stringBuilder.toString());

            for (String cryptoCurrency : cryptoCurrencies) {
                final HashMap<String, ExchangeRatesItem> exchangeRate = new HashMap<>();
                final JSONObject cryptoObject = (JSONObject) jsonObject.get(cryptoCurrency);
                for (String fiatCurrency : fiatCurrencies) {
                    final String fiatCurrencyValue = cryptoObject.getString(fiatCurrency);
                    exchangeRate.put(fiatCurrency, new ExchangeRatesItem(cryptoCurrency, fiatCurrency, fiatCurrencyValue));
                }
                exchangeRates.put(cryptoCurrency, exchangeRate);
            }

            DBHelper.putExchangeRates(exchangeRates);
        } catch (Exception e) {
            Toast.makeText(ApplicationLoader.applicationContext, "Обновить курсы валют не удалось", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return exchangeRates;
    }
}