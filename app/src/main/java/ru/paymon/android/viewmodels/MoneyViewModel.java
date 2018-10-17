package ru.paymon.android.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import org.bitcoinj.wallet.Wallet;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ru.paymon.android.NotificationManager;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.models.EthereumWallet;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.models.NonEmptyWalletItem;
import ru.paymon.android.models.PaymonWallet;
import ru.paymon.android.models.TransactionItem;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class MoneyViewModel extends AndroidViewModel implements NotificationManager.IListener {
    private MutableLiveData<ArrayList<ExchangeRatesItem>> exchangeRatesData;
    private MutableLiveData<ArrayList<WalletItem>> walletsData;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<String> ethereumBalance;
    private MutableLiveData<ArrayList<TransactionItem>> transactionsData;
    private MutableLiveData<Integer> maxGasPriceData = new MutableLiveData<>();
    private MutableLiveData<Integer> midGasPriceData = new MutableLiveData<>();
    private final WalletApplication application;
//    private BlockchainStateLiveData blockchainState;
//    private WalletBalanceLiveData balance;
//    private SelectedExchangeRateLiveData exchangeRate;


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

    //    public BlockchainStateLiveData getBlockchainState() {
//        if (blockchainState == null)
//            blockchainState = new BlockchainStateLiveData(application);
//        return blockchainState;
//    }
//
//    public WalletBalanceLiveData getBalance() {
//        if (balance == null)
//            balance = new WalletBalanceLiveData(application);
//        return balance;
//    }
//
//    public SelectedExchangeRateLiveData getExchangeRate() {
//        if (exchangeRate == null)
//            exchangeRate = new SelectedExchangeRateLiveData(application);
//        return exchangeRate;
//    }


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

    public LiveData<String> getEthereumBalanceData() {
        if (ethereumBalance == null)
            ethereumBalance = new MutableLiveData<>();
//        loadEthereumBalanceData();
        return ethereumBalance;
    }

    public LiveData<ArrayList<TransactionItem>> getTranscationsData() {
        if (transactionsData == null)
            transactionsData = new MutableLiveData<>();
//        loadEthereumTransactionsData();
        return transactionsData;
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

            EthereumWallet ethereumWallet = application.getEthereumWallet();
            if (ethereumWallet != null) {
                Log.e("AAA", ethereumWallet.publicAddress);
                BigInteger balance = application.getEthereumBalance();
                if (balance != null)
                    walletItems.add(new NonEmptyWalletItem(ETH_CURRENCY_VALUE, balance.toString(), ethereumWallet.publicAddress));//TODO:convert balance
                else
                    walletItems.add(new NonEmptyWalletItem(ETH_CURRENCY_VALUE, "0", ethereumWallet.publicAddress));
            } else {
                walletItems.add(new WalletItem(ETH_CURRENCY_VALUE));
            }

            Wallet bitcoinWallet = application.getBitcoinWallet();
            if (bitcoinWallet == null) {
                walletItems.add(new WalletItem(BTC_CURRENCY_VALUE));
            } else {
                walletItems.add(new NonEmptyWalletItem(BTC_CURRENCY_VALUE, bitcoinWallet.getBalance().toString(), bitcoinWallet.currentReceiveAddress().toString()));
            }

            PaymonWallet paymonWallet = application.getPaymonWallet();
            if(paymonWallet== null) {
                walletItems.add(new WalletItem(PMNT_CURRENCY_VALUE));
            }else{
//                walletItems.add(new NonEmptyWalletItem(PMNT_CURRENCY_VALUE, paymonWallet.getBalance().toString(), paymonWallet.currentReceiveAddress().toString()));
            }
//            if (User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD != null) {
//                boolean isWalletLoaded = Ethereum.getInstance().loadWallet(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD);
//                if (isWalletLoaded) {
//                    BigDecimal balance = Ethereum.getInstance().getBalance();
//                    if (balance != null) {
//                        ethereumBalance.postValue(balance.toString());
//                        User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS = Ethereum.getInstance().getAddress();
//                        User.CLIENT_MONEY_ETHEREUM_WALLET_PRIVATE_ADDRESS = Ethereum.getInstance().getPrivateKey();
//                        User.saveConfig();
//                        walletItems.add(new NonEmptyWalletItem(cryptoCurrency, balance.toString(), User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS));
//                    }
//                } /*else { //TODO: обрабатывать ситуацию когда кошелек не получилось загрузить
//                    walletItems.add(new WalletItem(cryptoCurrency));
//                }*/
//            } else {
//                walletItems.add(new WalletItem(cryptoCurrency));
//            }

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
                e.printStackTrace();
            }
            showProgress.postValue(false);
        });
    }

//    private void loadEthereumBalanceData() {
//        Utils.stageQueue.postRunnable(() -> {
//            showProgress.postValue(true);
//            BigDecimal walletBalance = Ethereum.getInstance().getBalance();
//            if (walletBalance != null) {
//                ethereumBalance.postValue(walletBalance.toString());
//            }
//            showProgress.postValue(false);
//        });
//    }

//    private void loadEthereumTransactionsData() {
//        Utils.stageQueue.postRunnable(() -> {
//            showProgress.postValue(true);
//            final String address = Ethereum.getInstance().getAddress();
//            final String link = "http://api.etherscan.io/api?module=account&action=txlist&address=" + address + "&startblock=0&endblock=99999999&sort=desc&apikey=YourApiKeyToken";
//            final ArrayList<TransactionItem> transactionItems = new ArrayList<>();
//
//            try {
//                final HttpURLConnection httpsURLConnection = (HttpURLConnection) ((new URL(link).openConnection()));
//                httpsURLConnection.setConnectTimeout(20000);
//                httpsURLConnection.setDoOutput(true);
//                httpsURLConnection.connect();
//                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
//                final StringBuilder stringBuilder = new StringBuilder();
//                String response;
//                while ((response = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(response);
//                }
//                bufferedReader.close();
//
//                final JSONObject jsonObject = new JSONObject(stringBuilder.toString());
//                final JSONArray jsonArray = jsonObject.getJSONArray("result");
//
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    JSONObject transcationObj = jsonArray.getJSONObject(i);
//                    String timestamp = Utils.formatDateTime(transcationObj.getLong("timeStamp"), false);
//                    String hash = transcationObj.getString("hash");
//                    String from = transcationObj.getString("from");
//                    String to = transcationObj.getString("to");
//                    String value = Convert.fromWei(transcationObj.getString("value"), Convert.Unit.ETHER).toString();
//                    String gasLimit = transcationObj.getString("gas");
//                    String gasPrice = transcationObj.getString("gasPrice");
//                    String gasUsed = transcationObj.getString("gasUsed");
//                    String status = transcationObj.getInt("txreceipt_status") == 1 ? "success" : "fail"; //TODO:String
//                    transactionItems.add(new TransactionItem(hash, status, timestamp, value, to, from, gasLimit, gasUsed, gasPrice));
//                }
//
//                transactionsData.postValue(transactionItems);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            showProgress.postValue(false);
//        });
//    }

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
}
