package ru.paymon.android.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import org.bitcoinj.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.utils.Convert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.User;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.models.EthereumWallet;
import ru.paymon.android.models.NonEmptyWalletItem;
import ru.paymon.android.models.PaymonWallet;
import ru.paymon.android.models.TransactionItem;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class MoneyViewModel extends AndroidViewModel implements NotificationManager.IListener {
    public String fiatCurrency = "USD";
    private MutableLiveData<List<ExchangeRate>> exchangeRatesData;
    private MutableLiveData<ArrayList<WalletItem>> walletsData;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<BigInteger> ethereumBalance;
    private MutableLiveData<BigInteger> paymonBalance;
    private MutableLiveData<ArrayList<TransactionItem>> ethereumTransactionsData;
    private MutableLiveData<ArrayList<TransactionItem>> paymonTransactionsData;
    private MutableLiveData<Integer> maxGasPriceData = new MutableLiveData<>();
    private MutableLiveData<Integer> midGasPriceData = new MutableLiveData<>();
    private final WalletApplication application;

    public MoneyViewModel(@NonNull Application application) {
        super(application);
        this.application = (WalletApplication) application;
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.BITCOIN_WALLET_CREATED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.ETHEREUM_WALLET_CREATED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.PAYMON_WALLET_CREATED);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.BITCOIN_WALLET_CREATED);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.ETHEREUM_WALLET_CREATED);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.PAYMON_WALLET_CREATED);
    }

    public LiveData<List<ExchangeRate>> getExchangeRatesData() {
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

    public LiveData<BigInteger> getEthereumBalanceData() {
        if (ethereumBalance == null)
            ethereumBalance = new MutableLiveData<>();
        loadEthereumBalanceData();
        return ethereumBalance;
    }

    public LiveData<BigInteger> getPaymonBalanceData() {
        if (paymonBalance == null)
            paymonBalance = new MutableLiveData<>();
        loadPaymonBalanceData();
        return paymonBalance;
    }

    public LiveData<ArrayList<TransactionItem>> getEthereumTranscationsData() {
        if (ethereumTransactionsData == null)
            ethereumTransactionsData = new MutableLiveData<>();
        loadEthereumTransactionsData();
        return ethereumTransactionsData;
    }

    public LiveData<ArrayList<TransactionItem>> getPaymonTranscationsData() {
        if (paymonTransactionsData == null)
            paymonTransactionsData = new MutableLiveData<>();
        loadPaymonTransactionsData();
        return paymonTransactionsData;
    }

    public void updateMidAndMaxGasPriceData() {
        loadGasPriceData();
    }

    public LiveData<Integer> getMidGasPriceData() {
        return midGasPriceData;
    }

    public LiveData<Integer> getMaxGasPriceData() {
        return maxGasPriceData;
    }

    public LiveData<Boolean> getProgressState() {
        return showProgress;
    }

    private void loadWalletsData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            final ArrayList<WalletItem> walletItems = new ArrayList<>();

            final EthereumWallet ethereumWallet = application.getEthereumWallet();
            final PaymonWallet paymonWallet = application.getPaymonWallet();
            final Wallet bitcoinWallet = application.getBitcoinWallet();

            if (ethereumWallet != null) {
                BigInteger balance = application.getEthereumBalance();
                if (balance != null) {
                    ExchangeRate exchangeRate = ApplicationLoader.db.exchangeRatesDao().getExchangeRatesByFiatAndCryptoCurrecy(fiatCurrency, ETH_CURRENCY_VALUE);
                    if (exchangeRate != null) {
                        String fiatBalance = WalletApplication.convertEthereumToFiat(balance, exchangeRate.value);
                        walletItems.add(new NonEmptyWalletItem(ETH_CURRENCY_VALUE, Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER).toString(), fiatCurrency, fiatBalance));
                    } else {
                        walletItems.add(new NonEmptyWalletItem(ETH_CURRENCY_VALUE, "0", fiatCurrency, "0"));
                    }
                } else {
                    walletItems.add(new NonEmptyWalletItem(ETH_CURRENCY_VALUE, "0", fiatCurrency, "0"));
                }
            }

            if (paymonWallet != null) {
                BigInteger balance = application.getPaymonBalance();
                if (balance != null) {
                    ExchangeRate exchangeRate = ApplicationLoader.db.exchangeRatesDao().getExchangeRatesByFiatAndCryptoCurrecy(fiatCurrency, PMNT_CURRENCY_VALUE);
                    if (exchangeRate != null) {
                        String fiatBalance = WalletApplication.convertPaymonToFiat(balance, exchangeRate.value);
                        walletItems.add(new NonEmptyWalletItem(PMNT_CURRENCY_VALUE, paymonWallet.balance, fiatCurrency, fiatBalance));
                    } else {
                        walletItems.add(new NonEmptyWalletItem(PMNT_CURRENCY_VALUE, "0", fiatCurrency, "0"));
                    }
                } else {
                    walletItems.add(new NonEmptyWalletItem(PMNT_CURRENCY_VALUE, "0", fiatCurrency, "0"));
                }
            }

            if (bitcoinWallet != null && User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD != null) {
                ApplicationLoader.applicationHandler.post(() -> {
                    String balance = bitcoinWallet.getBalance().toPlainString();
                    ExchangeRate exchangeRate = ApplicationLoader.db.exchangeRatesDao().getExchangeRatesByFiatAndCryptoCurrecy(fiatCurrency, BTC_CURRENCY_VALUE);
                    if (exchangeRate != null) {
                        String fiatBalance = WalletApplication.convertBitcoinToFiat(balance, exchangeRate.value);
                        walletItems.add(new NonEmptyWalletItem(BTC_CURRENCY_VALUE, balance, fiatCurrency, fiatBalance));
                    } else {
                        walletItems.add(new NonEmptyWalletItem(BTC_CURRENCY_VALUE, balance, fiatCurrency, "0"));
                    }
                });
            }

            if (bitcoinWallet != null && User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD == null)
                walletItems.add(new WalletItem(BTC_CURRENCY_VALUE));

            if (paymonWallet == null)
                walletItems.add(new WalletItem(PMNT_CURRENCY_VALUE));

            if (ethereumWallet == null)
                walletItems.add(new WalletItem(ETH_CURRENCY_VALUE));

            walletsData.postValue(walletItems);
            showProgress.postValue(false);
        });
    }

    private void loadExchangeRatesData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            final String[] cryptoCurrencies = new String[]{"BTC", "ETH", "PMNT"};
            final String[] fiatCurrencies = new String[]{"USD", "EUR", "RUB"};
            final String link = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=BTC,ETH,PMNT&tsyms=USD,EUR,RUB," + Currency.getInstance(Locale.getDefault()).getCurrencyCode().toUpperCase();
            final ArrayList<ExchangeRate> exchangeRatesItems = new ArrayList<>();

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

                int id = 0;
                for (String cryptoCurrency : cryptoCurrencies) {
                    final JSONObject cryptoObject = (JSONObject) jsonObject.get(cryptoCurrency);
                    for (String fiatCurrency : fiatCurrencies) {
                        exchangeRatesItems.add(new ExchangeRate(++id, fiatCurrency, cryptoCurrency, String.format(Locale.US, "%.09f", cryptoObject.getDouble(fiatCurrency)).replaceAll("\\.(.*?)0+$", ".$1").replaceAll("\\.$", "")));
                    }
                }

                if (exchangeRatesItems.size() > 0) {
                    ApplicationLoader.db.exchangeRatesDao().deleteAll();
                    ApplicationLoader.db.exchangeRatesDao().insertList(exchangeRatesItems);
                    exchangeRatesData.postValue(exchangeRatesItems);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            showProgress.postValue(false);
        });
    }

    private void loadEthereumBalanceData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            BigInteger walletBalance = application.getEthereumBalance();
            if (walletBalance != null) {
                ethereumBalance.postValue(walletBalance);
            }
            showProgress.postValue(false);
        });
    }

    private void loadPaymonBalanceData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            BigInteger walletBalance = application.getPaymonBalance();
            if (walletBalance != null) {
                paymonBalance.postValue(walletBalance);
            }
            showProgress.postValue(false);
        });
    }

    private void loadGasPriceData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            try {
                final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) ((new URL("https://www.etherchain.org/api/gasPriceOracle").openConnection()));
                httpsURLConnection.setConnectTimeout(20000);
                httpsURLConnection.connect();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                final StringBuilder stringBuilder = new StringBuilder();
                String response;
                while ((response = bufferedReader.readLine()) != null) {
                    stringBuilder.append(response);
                }
                bufferedReader.close();

                final JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                final int maxGasPrice = (int) Float.parseFloat(jsonObject.getString("fastest"));
                final int midGasPrice = (int) Float.parseFloat(jsonObject.getString("standard"));

                maxGasPriceData.postValue(maxGasPrice);
                midGasPriceData.postValue(midGasPrice);
            } catch (Exception e) {
                e.printStackTrace();
                maxGasPriceData.postValue(100);
                midGasPriceData.postValue(1);
            }
            showProgress.postValue(false);
        });
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.ETHEREUM_WALLET_CREATED || event == NotificationManager.NotificationEvent.BITCOIN_WALLET_CREATED || event == NotificationManager.NotificationEvent.PAYMON_WALLET_CREATED) {
            loadWalletsData();
        }
    }

    private void loadEthereumTransactionsData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            final String address = application.getEthereumWallet().publicAddress;
            final String link = "http://api.etherscan.io/api?module=account&action=txlist&address=" + address + "&startblock=0&endblock=99999999&sort=desc&apikey=YourApiKeyToken";
            final ArrayList<TransactionItem> transactionItems = new ArrayList<>();

            try {
                final HttpURLConnection httpsURLConnection = (HttpURLConnection) ((new URL(link).openConnection()));
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
                final JSONArray jsonArray = jsonObject.getJSONArray("result");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject transcationObj = jsonArray.getJSONObject(i);
                    String timestamp = Utils.formatDateTime(transcationObj.getLong("timeStamp"), false);
                    String hash = transcationObj.getString("hash");
                    String from = transcationObj.getString("from");
                    String to = transcationObj.getString("to");
                    String value = Convert.fromWei(transcationObj.getString("value"), Convert.Unit.ETHER).toString();
                    String gasLimit = transcationObj.getString("gas");
                    String gasPrice = transcationObj.getString("gasPrice");
                    String gasUsed = transcationObj.getString("gasUsed");
                    String status = transcationObj.getInt("txreceipt_status") == 1 ? "success" : "fail"; //TODO:String
                    transactionItems.add(new TransactionItem(hash, status, timestamp, value, to, from, gasLimit, gasUsed, gasPrice));
                }

                ethereumTransactionsData.postValue(transactionItems);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showProgress.postValue(false);
        });
    }

    private void loadPaymonTransactionsData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            final String address = application.getPaymonWallet().publicAddress;
            final String link = "http://api.etherscan.io/api?module=account&action=txlist&address=" + address + "&startblock=0&endblock=99999999&sort=desc&apikey=YourApiKeyToken";
            final ArrayList<TransactionItem> transactionItems = new ArrayList<>();

            try {
                final HttpURLConnection httpsURLConnection = (HttpURLConnection) ((new URL(link).openConnection()));
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
                final JSONArray jsonArray = jsonObject.getJSONArray("result");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject transcationObj = jsonArray.getJSONObject(i);
                    String timestamp = Utils.formatDateTime(transcationObj.getLong("timeStamp"), false);
                    String hash = transcationObj.getString("hash");
                    String from = transcationObj.getString("from");
                    String to = transcationObj.getString("to");
                    String value = Convert.fromWei(transcationObj.getString("value"), Convert.Unit.ETHER).toString();
                    String gasLimit = transcationObj.getString("gas");
                    String gasPrice = transcationObj.getString("gasPrice");
                    String gasUsed = transcationObj.getString("gasUsed");
                    String status = transcationObj.getInt("txreceipt_status") == 1 ? "success" : "fail"; //TODO:String
                    transactionItems.add(new TransactionItem(hash, status, timestamp, value, to, from, gasLimit, gasUsed, gasPrice));
                }

                ethereumTransactionsData.postValue(transactionItems);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showProgress.postValue(false);
        });
    }
}