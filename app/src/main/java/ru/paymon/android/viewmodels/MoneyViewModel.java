package ru.paymon.android.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.User;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.models.NonEmptyWalletItem;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.Utils;

public class MoneyViewModel extends ViewModel {
    private MutableLiveData<ArrayList<ExchangeRatesItem>> exchangeRatesData;
    private MutableLiveData<ArrayList<WalletItem>> walletsData;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();

    public LiveData<ArrayList<ExchangeRatesItem>> getExchangeRatesData() {
        if (exchangeRatesData == null)
            exchangeRatesData = new MutableLiveData<>();
        loadExchangeRatesData();
        return exchangeRatesData;
    }

    public LiveData<ArrayList<WalletItem>> getWalletsData() {
        if (walletsData == null)
            walletsData = new MutableLiveData<>();
        loadWalletsData();
        return walletsData;
    }

    public LiveData<Boolean> getProgressState() {
        return showProgress;
    }

    private void loadWalletsData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            final ArrayList<WalletItem> walletItems = new ArrayList<>();
            final String cryptoCurrency = "ETH";
            if (User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD != null) {

                boolean isWalletLoaded = Ethereum.getInstance().loadWallet(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD);
                if (isWalletLoaded) {
                    BigDecimal balance = Ethereum.getInstance().getBalance();
                    if (balance != null) {
                        User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE = balance.toString();
                        User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS = Ethereum.getInstance().getAddress();
                        User.CLIENT_MONEY_ETHEREUM_WALLET_PRIVATE_ADDRESS = Ethereum.getInstance().getPrivateKey();
                        User.saveConfig();
                        walletItems.add(new NonEmptyWalletItem(cryptoCurrency, User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE, User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS));
                    }
                } else {
                    walletItems.add(new WalletItem(cryptoCurrency));
                }
            } else {
                walletItems.add(new WalletItem(cryptoCurrency));
            }
            walletsData.postValue(walletItems);
            showProgress.postValue(false);
        });
    }

    private void loadExchangeRatesData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            final String[] cryptoCurrencies = new String[]{"BTC", "ETH", "PMNT"};
            final String[] fiatCurrencies = new String[]{"USD", "EUR"};
            final String link = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=BTC,ETH,PMNT&tsyms=USD,EUR," + Currency.getInstance(Locale.getDefault()).getCurrencyCode().toUpperCase();
            final ArrayList<ExchangeRatesItem> exchangeRatesItems = new ArrayList<>();

            try {
                final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) ((new URL(link).openConnection()));
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
                    final JSONObject cryptoObject = (JSONObject) jsonObject.get(cryptoCurrency);
                    for (String fiatCurrency : fiatCurrencies) {
                        final String fiatCurrencyValue = cryptoObject.getString(fiatCurrency);
                        exchangeRatesItems.add(new ExchangeRatesItem(cryptoCurrency, fiatCurrency, Float.parseFloat(fiatCurrencyValue)));
                    }
                }

                exchangeRatesData.postValue(exchangeRatesItems);
            } catch (Exception e) {
                Toast.makeText(ApplicationLoader.applicationContext, "Обновить курсы валют не удалось", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            showProgress.postValue(false);
        });
    }
}
