//package ru.paymon.android.view;
//
//import android.annotation.SuppressLint;
//import android.graphics.Typeface;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.FrameLayout;
//import android.widget.Spinner;
//import android.widget.TextSwitcher;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.web3j.crypto.CipherException;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Currency;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Locale;
//
//import javax.net.ssl.HttpsURLConnection;
//
//import ru.paymon.android.ApplicationLoader;
//import ru.paymon.android.NotificationManager;
//import ru.paymon.android.R;
//import ru.paymon.android.User;
//import ru.paymon.android.adapters.CryptoWalletsAdapter;
//import ru.paymon.android.adapters.ExchangeRatesAdapter;
//import ru.paymon.android.gateway.Ethereum;
//import ru.paymon.android.models.ExchangeRatesItem;
//import ru.paymon.android.models.WalletItem;
//import ru.paymon.android.utils.Utils;
//
//public class FragmentMoney extends Fragment implements NotificationManager.IListener {
//    private static FragmentMoney instance;
//    private RecyclerView walletsRecView;
//    private RecyclerView exchangeRatesRecView;
//    private TextSwitcher loader;
//    private CryptoWalletsAdapter cryptoWalletsAdapter;
//    private Spinner spinner;
//    private HashSet<String> fiatCurrencies = new HashSet<>();
//    private HashMap<String, HashMap<String, String>> courses = new HashMap<>();
//    private HashMap<String, HashMap<String, ExchangeRatesItem>> exchangeRateItems = new HashMap<>();
//
//    public static synchronized FragmentMoney newInstance() {
//        instance = new FragmentMoney();
//        return instance;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_money, container, false);
//
//        walletsRecView = (RecyclerView) view.findViewById(R.id.fragment_money_wallets);
//        exchangeRatesRecView = (RecyclerView) view.findViewById(R.id.fragment_money_exchange_rates);
//        loader = (TextSwitcher) view.findViewById(R.id.fragment_money_loader);
//        spinner = (Spinner) view.findViewById(R.id.fragment_money_spinner);
//        TextView updateButton = (TextView) view.findViewById(R.id.fragment_money_update);
//
//        loader.setVisibility(View.GONE);
//
//        walletsRecView.setHasFixedSize(true);
//        walletsRecView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        exchangeRatesRecView.setHasFixedSize(true);
//        exchangeRatesRecView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        final Animation slideInLeftAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
//        final Animation slideOutRightAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
//
//        loader.setInAnimation(slideInLeftAnimation);
//        loader.setOutAnimation(slideOutRightAnimation);
//
//        loader.setFactory(() -> {
//            final TextView textView = new TextView(getActivity());
//            textView.setTextSize(24);
//            textView.setGravity(Gravity.CENTER);
//            textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
//            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.blue_dark));
//            textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
//            return textView;
//        });
//
//        fiatCurrencies.add("USD");
//        fiatCurrencies.add("EUR");
//        fiatCurrencies.add(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fiatCurrencies.toArray(new String[fiatCurrencies.size()]));
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        spinner.setAdapter(adapter);
//        spinner.setSelection(0);
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                //TODO:парсинг курсов и смена баланса и валюты у кошелей
//                changeFiatCurrency();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//
//
//        updateButton.setOnClickListener(view1 -> {
//            init();
//            initWallets();
//        });
//
//
//        return view;
//    }
//
//    private LoaderHandler loaderHandler;
//
//    private void startLoaderAnimation() {
//        loaderHandler = new LoaderHandler();
//        loaderHandler.start();
//    }
//
//    private void stopLoaderAnimation() {
//        loader.setVisibility(View.GONE);
//        loaderHandler.halt();
//    }
//
//    private void changeFiatCurrency() {
//        final String selectedFiatCurrency = spinner.getSelectedItem().toString();
//        ArrayList<ExchangeRatesItem> list = new ArrayList<>();
//        for (String cryptoCurKey : exchangeRateItems.keySet()) {
//            HashMap<String, ExchangeRatesItem> map = exchangeRateItems.get(cryptoCurKey);
//            list.add(map.get(selectedFiatCurrency));
//        }
//
//        ExchangeRatesAdapter exchangeRatesAdapter = new ExchangeRatesAdapter(list);
//        exchangeRatesRecView.setAdapter(exchangeRatesAdapter);
//        initWallets();
//    }
//
//    private void initWallets() {
//        final String selectedFiatCurrency = spinner.getSelectedItem().toString();
//
//        Utils.stageQueue.postRunnable(() -> {
//            ArrayList<WalletItem> walletItems = new ArrayList<>();
//
//            if (User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD != null) {
//                try {
//                    Ethereum.getInstance().loadWallet(User.currentUser.id, User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD);
//                    Ethereum.getInstance().getBalance();
////                    final String cryptoBalance = Ethereum.getInstance().CRYPTO_BALANCE;
////                    final String fiatBalance = Ethereum.getInstance().calculateFiatBalance(courses.get("ETH").get(selectedFiatCurrency));
////                    walletItems.add(new WalletItem("ETH", selectedFiatCurrency, cryptoBalance, fiatBalance, false));
//                    walletItems.add(new WalletItem("ETH", selectedFiatCurrency, "0.00", "0.00", false)); //TODO:баланс и фиат валюта
//                } catch (IOException ioex) {
//                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, "Файл Ethereum кошелька поврежден", Toast.LENGTH_LONG).show());
//                    walletItems.add(new WalletItem("ETH", selectedFiatCurrency, "0.00", "0.00", true));
//                } catch (CipherException cex) {
//                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, "Пароль Ethereum кошелька не верный", Toast.LENGTH_LONG).show());
//                    walletItems.add(new WalletItem("ETH", selectedFiatCurrency, "0.00", "0.00", true));
//                }
//            } else {
//                walletItems.add(new WalletItem("ETH", "RUB", "0.00", "0.00", true));
//                //TODO:затестить и если что спрашивать пароль и ток потом загружать кошель
//            }
//
//
//            //TODO:получение инфы о кошельке юзера с etherscan
//            walletItems.add(new WalletItem("BTC", selectedFiatCurrency, "0.00", "0.00", false));
//            walletItems.add(new WalletItem("PMNT", selectedFiatCurrency, "0.00", "0.00", false));
//
//            cryptoWalletsAdapter = new CryptoWalletsAdapter(walletItems, getActivity());
//            ApplicationLoader.applicationHandler.post(() -> walletsRecView.setAdapter(cryptoWalletsAdapter));
//        });
//    }
//
//
//    @SuppressLint("StaticFieldLeak")
//    private void init() {
//        startLoaderAnimation();
//
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                String[] cryptoCurrencies = new String[] {"BTC,ETH,PMNT"};
//
//                String link = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=" + cryptoCurrencies.toString().replace("[", "").replace("]", "").replace(" ", "") + "&tsyms=" + fiatCurrencies.toString().replace("[", "").replace("]", "").replace(" ", "");
//
//                try {
//                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) ((new URL(link.toString()).openConnection()));
//                    httpsURLConnection.setConnectTimeout(20000);
//                    httpsURLConnection.setDoOutput(true);
//                    httpsURLConnection.connect();
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
//                    StringBuilder stringBuilder = new StringBuilder();
//                    String response;
//                    while ((response = bufferedReader.readLine()) != null) {
//                        stringBuilder.append(response);
//                    }
//                    bufferedReader.close();
//
//                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
//
//                    courses.clear();
//                    exchangeRateItems.clear();
//                    for (String cryptoCurrency : cryptoCurrencies) {
//                        HashMap<String, String> course = new HashMap<>();
//                        HashMap<String, ExchangeRatesItem> exchangeRate = new HashMap<>();
//                        JSONObject cryptoObject = (JSONObject) jsonObject.get(cryptoCurrency);
//                        for (String fiatCurrency : fiatCurrencies) {
//                            String fiatCurrencyValue = cryptoObject.getString(fiatCurrency);
//                            course.put(fiatCurrency, fiatCurrencyValue);
//                            exchangeRate.put(fiatCurrency, new ExchangeRatesItem(cryptoCurrency, fiatCurrency, fiatCurrencyValue));
//                        }
//                        exchangeRateItems.put(cryptoCurrency, exchangeRate);
//                        courses.put(cryptoCurrency, course);
//                    }
//                } catch (JSONException | IOException ignored) {
//                    ignored.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void voids) {
//                super.onPostExecute(voids);
//
//                if (courses.size() <= 0) {
//                    Toast.makeText(ApplicationLoader.applicationContext, "Обновить курсы валют не удалось", Toast.LENGTH_LONG).show();
//                    //TODO:инициализация кошелей с пустым фиатным балансом (или с кэша)
//                } else {
//                    String selectedFiatCurrency = spinner.getSelectedItem().toString();
//
//                    ArrayList<ExchangeRatesItem> list = new ArrayList<>();
//                    for (String cryptoCurKey : exchangeRateItems.keySet()) {
//                        HashMap<String, ExchangeRatesItem> map = exchangeRateItems.get(cryptoCurKey);
//                        list.add(map.get(selectedFiatCurrency));
//                    }
//
//                    ExchangeRatesAdapter exchangeRatesAdapter = new ExchangeRatesAdapter(list);
//                    exchangeRatesRecView.setAdapter(exchangeRatesAdapter);
//
//                    initWallets();
//                }
//                stopLoaderAnimation();
//            }
//        }.execute();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didLoadEthereumWallet);
//        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didLoadPaymonWallet);
//        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didLoadBitcoinWallet);
//        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
//        Utils.hideActionBar(getActivity());
//        init();
////        initWallets();
//    }
//
//    @Override
//    public void onPause() {
//        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didLoadEthereumWallet);
//        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didLoadPaymonWallet);
//        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didLoadBitcoinWallet);
//        super.onPause();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
//        //TODO:баланс и фиат валюта
//        final String selectedFiatCurrency = spinner.getSelectedItem().toString();
//
//        if (event == NotificationManager.NotificationEvent.didLoadEthereumWallet) {
//            final String fiatBalance = Ethereum.getInstance().calculateFiatBalance(courses.get("ETH").get(selectedFiatCurrency));
//            final String cryptoBalance = Ethereum.getInstance().CRYPTO_BALANCE;
//            cryptoWalletsAdapter.walletItems.set(2, new WalletItem("ETH", selectedFiatCurrency, cryptoBalance, fiatBalance, false));
////            cryptoWalletsAdapter.walletItems.set(2, new WalletItem("ETH", "RUB", "0.00", "0.00", false));
//        } else if (event == NotificationManager.NotificationEvent.didLoadBitcoinWallet) {
//            cryptoWalletsAdapter.walletItems.set(0, new WalletItem("BTC", "RUB", "0.00", "0.00", false));
//        } else if (event == NotificationManager.NotificationEvent.didLoadPaymonWallet) {
//            cryptoWalletsAdapter.walletItems.set(1, new WalletItem("PMNT", "RUB", "0.00", "0.00", false));
//        }
//        cryptoWalletsAdapter.notifyDataSetChanged();
//    }
//
//    private class LoaderHandler extends Thread implements Runnable {
//        boolean running = true;
//
//        @Override
//        public void run() {
//            int loaderIndex = 0;
//            String[] loaderValues = new String[]{getActivity().getString(R.string.loading_one), getActivity().getString(R.string.loading_two), getActivity().getString(R.string.loading_three)};
//            ApplicationLoader.applicationHandler.post(() -> loader.setVisibility(View.VISIBLE));
//            while (running) {
//                switch (loaderIndex) {
//                    case 0:
//                        loaderIndex = 1;
//                        ApplicationLoader.applicationHandler.post(() -> loader.setText(loaderValues[0]));
//                        break;
//                    case 1:
//                        loaderIndex = 2;
//                        ApplicationLoader.applicationHandler.post(() -> loader.setText(loaderValues[1]));
//                        break;
//                    case 2:
//                        ApplicationLoader.applicationHandler.post(() -> loader.setText(loaderValues[2]));
//                        loaderIndex = 0;
//                        break;
//                }
//
//                try {
//                    Thread.sleep(450);
//                } catch (InterruptedException e) {
//
//                }
//            }
//        }
//
//        public void halt() {
//            running = false;
//        }
//    }
//}
