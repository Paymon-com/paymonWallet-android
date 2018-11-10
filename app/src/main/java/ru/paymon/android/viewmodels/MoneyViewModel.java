package ru.paymon.android.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.ExchangeRatesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.models.EthTransactionItem;
import ru.paymon.android.models.EthereumWallet;
import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.models.NonEmptyWalletItem;
import ru.paymon.android.models.PaymonWallet;
import ru.paymon.android.models.PmntTransactionItem;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.room.AppDatabase;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.User.CLIENT_BASIC_DATE_FORMAT_IS_24H;
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
    private MutableLiveData<BigInteger> paymonEthBalance;
    private MutableLiveData<ArrayList<EthTransactionItem>> ethereumTransactionsData;
    private MutableLiveData<ArrayList<PmntTransactionItem>> paymonTransactionsData;
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
//        loadExchangeRatesData();
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

    public LiveData<BigInteger> getPaymonEthBalanceData() {
        if (paymonEthBalance == null)
            paymonEthBalance = new MutableLiveData<>();
        loadPaymonEthBalanceData();
        return paymonEthBalance;
    }

    public LiveData<ArrayList<EthTransactionItem>> getEthereumTranscationsData() {
        if (ethereumTransactionsData == null)
            ethereumTransactionsData = new MutableLiveData<>();
        loadEthereumTransactionsData();
        return ethereumTransactionsData;
    }

    public LiveData<ArrayList<PmntTransactionItem>> getPaymonTranscationsData() {
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
        loadExchangeRatesData();
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            final ArrayList<WalletItem> walletItems = new ArrayList<>();

            final EthereumWallet ethereumWallet = application.getEthereumWallet();
            final PaymonWallet paymonWallet = application.getPaymonWallet();
            final Wallet bitcoinWallet = application.getBitcoinWallet();

            if (ethereumWallet != null) {
                BigInteger balance = application.getEthereumBalance();
                if (balance != null) {
                    ExchangeRate exchangeRate = ExchangeRatesManager.getInstance().getExchangeRatesByFiatAndCryptoCurrecy(fiatCurrency, ETH_CURRENCY_VALUE);
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
                    ExchangeRate exchangeRate = ExchangeRatesManager.getInstance().getExchangeRatesByFiatAndCryptoCurrecy(fiatCurrency, PMNT_CURRENCY_VALUE);
                    if (exchangeRate != null) {
                        String fiatBalance = WalletApplication.convertPaymonToFiat(balance, exchangeRate.value);
                        walletItems.add(new NonEmptyWalletItem(PMNT_CURRENCY_VALUE, Convert.fromWei(new BigDecimal(balance), Convert.Unit.GWEI).toString(), fiatCurrency, fiatBalance));
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
                    ExchangeRate exchangeRate = ExchangeRatesManager.getInstance().getExchangeRatesByFiatAndCryptoCurrecy(fiatCurrency, BTC_CURRENCY_VALUE);
                    if (exchangeRate != null) {
                        String fiatBalance = WalletApplication.convertBitcoinToFiat(balance, exchangeRate.value);
                        walletItems.add(new NonEmptyWalletItem(BTC_CURRENCY_VALUE, balance, fiatCurrency, fiatBalance));
                    } else {
                        walletItems.add(new NonEmptyWalletItem(BTC_CURRENCY_VALUE, balance, fiatCurrency, "0"));
                    }
                });
            }

            if (bitcoinWallet == null && User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD == null)
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
                    for (String fiatCurrency : Config.fiatCurrencies) {
                        exchangeRatesItems.add(new ExchangeRate(++id, fiatCurrency, cryptoCurrency, String.format(Locale.US, "%.09f", cryptoObject.getDouble(fiatCurrency)).replaceAll("\\.(.*?)0+$", ".$1").replaceAll("\\.$", "")));
                    }
                }

                if (exchangeRatesItems.size() > 0) {
//                    Executors.newSingleThreadExecutor().submit(() -> {
                    AppDatabase.getDatabase().exchangeRatesDao().deleteAll();
                    AppDatabase.getDatabase().exchangeRatesDao().insertList(exchangeRatesItems);
//                    });
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

    private void loadPaymonEthBalanceData() {
        Utils.stageQueue.postRunnable(() -> {
            showProgress.postValue(true);
            BigInteger walletBalance = application.getPaymonEthBalance();
            if (walletBalance != null) {
                paymonEthBalance.postValue(walletBalance);
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
            final ArrayList<EthTransactionItem> transactionItems = new ArrayList<>();

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
                    Long timestamp = transcationObj.getLong("timeStamp");
                    String date = (String) DateFormat.format(CLIENT_BASIC_DATE_FORMAT_IS_24H ? "d MMM yyyy HH:mm" : "d MMM yyyy hh:mm aa", new Date(timestamp * 1000));
                    String hash = transcationObj.getString("hash");
                    String from = transcationObj.getString("from");
                    String to = transcationObj.getString("to");
                    String value = Convert.fromWei(transcationObj.getString("value"), Convert.Unit.ETHER).toString() + " ETH";
                    String gasLimit = transcationObj.getString("gas");
                    String gasPrice = new BigDecimal(transcationObj.getString("gasPrice")).divide(new BigDecimal(Math.pow(10, 9))).toString() + " GWEI";
                    String gasUsed = transcationObj.getString("gasUsed");
                    String status = transcationObj.getInt("txreceipt_status") == 1 ? application.getApplicationContext().getString(R.string.other_success) : application.getApplicationContext().getString(R.string.other_fail);
                    transactionItems.add(new EthTransactionItem(hash, status, date, value, to, from, gasLimit, gasUsed, gasPrice));
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
            final String link = "http://api.etherscan.io/api?module=account&action=tokentx&address=" + address + "&startblock=0&endblock=999999999&sort=desc&apikey=YourApiKeyToken";
            final ArrayList<PmntTransactionItem> transactionItems = new ArrayList<>();

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
                    String tokenSymbol = transcationObj.getString("tokenSymbol");
                    if (!tokenSymbol.equals("PMNT")) continue;
                    Long timestamp = transcationObj.getLong("timeStamp");
                    String date = (String) DateFormat.format(CLIENT_BASIC_DATE_FORMAT_IS_24H ? "d MMM yyyy HH:mm" : "d MMM yyyy hh:mm aa", new Date(timestamp * 1000));
                    String hash = transcationObj.getString("hash");
                    String from = transcationObj.getString("from");
                    String to = transcationObj.getString("to");
                    String value = Convert.fromWei(transcationObj.getString("value"), Convert.Unit.GWEI).toString() + " PMNT";
                    String gasLimit = transcationObj.getString("gas");
                    String gasPrice = Convert.fromWei(transcationObj.getString("gasPrice"), Convert.Unit.GWEI) + " GWEI";
                    String gasUsed = transcationObj.getString("gasUsed");
                    transactionItems.add(new PmntTransactionItem(hash, date, value, to, from, gasLimit, gasUsed, gasPrice));

//                    try {
//                        String method = data.substring(0, 10);
//                        String toData = data.substring(10, 74);
//                        String valueData = data.substring(74);
//                        Method refMethod = TypeDecoder.class.getDeclaredMethod("decode", String.class, int.class, Class.class);
//                        refMethod.setAccessible(true);
//                        Address addressData = (Address) refMethod.invoke(null, toData, 0, Address.class);
//                        Uint256 amount = (Uint256) refMethod.invoke(null, valueData, 0, Uint256.class);
//                        String amountStr = Convert.fromWei(new BigDecimal(amount.getValue()), Convert.Unit.GWEI).toString();
//                        transactionItems.add(new PmntTransactionItem(hash, status, date, value, to, from, gasLimit, gasUsed, gasPrice, method, addressData.toString(), amountStr));
//                    } catch (Exception e) {
//                        transactionItems.add(new EthTransactionItem(hash, status, date, value, to, from, gasLimit, gasUsed, gasPrice));
//                    }
                }

                paymonTransactionsData.postValue(transactionItems);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showProgress.postValue(false);
        });
    }
}