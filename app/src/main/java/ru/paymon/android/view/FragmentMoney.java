package ru.paymon.android.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidviewhover.tools.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.DBHelper;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.adapters.CryptoWalletsAdapter;
import ru.paymon.android.adapters.ExchangeRatesAdapter;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.ExchangeRates;
import ru.paymon.android.utils.Utils;

public class FragmentMoney extends Fragment implements NotificationManager.IListener {
    private static FragmentMoney instance;
    private RecyclerView walletsRecView;
    private RecyclerView exchangeRatesRecView;
    private TextSwitcher loader;
    private CryptoWalletsAdapter cryptoWalletsAdapter;
    private ExchangeRatesAdapter exchangeRatesAdapter;
    private Spinner spinner;
    private LoaderHandler loaderHandler;
    private HashMap<String, HashMap<String, ExchangeRatesItem>> exchangeRates;

    public static synchronized FragmentMoney newInstance() {
        instance = new FragmentMoney();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_money, container, false);

        walletsRecView = (RecyclerView) view.findViewById(R.id.fragment_money_wallets);
        exchangeRatesRecView = (RecyclerView) view.findViewById(R.id.fragment_money_exchange_rates);
        loader = (TextSwitcher) view.findViewById(R.id.fragment_money_loader);
        spinner = (Spinner) view.findViewById(R.id.fragment_money_spinner);
        TextView updateButton = (TextView) view.findViewById(R.id.fragment_money_update);

        walletsRecView.setHasFixedSize(true);
        walletsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        exchangeRatesRecView.setHasFixedSize(true);
        exchangeRatesRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        final Animation slideInLeftAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        final Animation slideOutRightAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);

        loader.setInAnimation(slideInLeftAnimation);
        loader.setOutAnimation(slideOutRightAnimation);

        loader.setFactory(() -> {
            final TextView textView = new TextView(getActivity());
            textView.setTextSize(24);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.blue_dark));
            textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            return textView;
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, ExchangeRates.getInstance().fiatCurrencies.toArray(new String[ExchangeRates.getInstance().fiatCurrencies.size()]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeFiatCurrency();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        updateButton.setOnClickListener(view1 -> {
            initExchangeRatesAndWallets();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.showBottomBar(getActivity());
        Utils.hideActionBar(getActivity());
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didLoadEthereumWallet);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didLoadPaymonWallet);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didLoadBitcoinWallet);
        initExchangeRatesAndWallets();
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didLoadEthereumWallet);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didLoadPaymonWallet);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didLoadBitcoinWallet);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        initExchangeRatesAndWallets();
    }

    private void startLoaderAnimation() {
        loaderHandler = new LoaderHandler();
        loaderHandler.start();
    }

    private void stopLoaderAnimation() {
        loader.setVisibility(View.GONE);
        loaderHandler.halt();
    }

    private void initExchangeRatesAndWallets() {
        Utils.stageQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(this::startLoaderAnimation);
            final String currentFiatCurrency = spinner.getSelectedItem().toString();
            exchangeRates = DBHelper.getExchangeRates();
            ArrayList<ExchangeRatesItem> exchangeRatesItems = new ArrayList<>();
            for (final String cryptoCurrency : exchangeRates.keySet()) {
                final HashMap<String, ExchangeRatesItem> exchangeRate = exchangeRates.get(cryptoCurrency);
                final String value = exchangeRate.get(currentFiatCurrency).value;
                exchangeRatesItems.add(new ExchangeRatesItem(cryptoCurrency, currentFiatCurrency, value));
            }
            exchangeRatesAdapter = new ExchangeRatesAdapter(exchangeRatesItems);
            ApplicationLoader.applicationHandler.post(() -> exchangeRatesRecView.setAdapter(exchangeRatesAdapter));

            final ArrayList<WalletItem> walletItems = new ArrayList<>();
            if (User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD != null) {
                final String cryptoCurrency = "ETH";
                final String exchangeRate = exchangeRates.get(cryptoCurrency).get(currentFiatCurrency).value;

                boolean isWalletLoaded = Ethereum.getInstance().loadWallet(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD);
                if (isWalletLoaded)
                    walletItems.add(new WalletItem(cryptoCurrency, currentFiatCurrency, User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE, Ethereum.getInstance().calculateFiatBalance(exchangeRates.get("ETH").get(currentFiatCurrency).value), false));
                else
                    walletItems.add(new WalletItem(cryptoCurrency, currentFiatCurrency, "0.00", "0.00", true));
                Ethereum.getInstance().getBalance(
                        (responseBalance) -> {
                            final BigInteger bigInteger = Ethereum.getInstance().jsonToWei(responseBalance);
                            if (bigInteger != null) {
                                User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE = Ethereum.getInstance().weiToFriendlyString(bigInteger);
                                User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS = Ethereum.getInstance().getAddress();
                                User.CLIENT_MONEY_ETHEREUM_WALLET_PRIVATE_ADDRESS = Ethereum.getInstance().getPrivateKey();
                                User.saveConfig();
                                final String fiatBalance = Ethereum.getInstance().calculateFiatBalance(exchangeRate);
                                walletItems.add(new WalletItem(cryptoCurrency, currentFiatCurrency, User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE, fiatBalance, false));
                            }
                        },
                        (error) -> {
                            ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, "Баланс Ethereum кошелька получить не удалось!", Toast.LENGTH_LONG).show());
                            final String fiatBalance = Ethereum.getInstance().calculateFiatBalance(exchangeRate);
                            walletItems.add(new WalletItem(cryptoCurrency, currentFiatCurrency, User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE, fiatBalance, false));
                        });
            } else {
                walletItems.add(new WalletItem("ETH", currentFiatCurrency, "0.00", "0.00", true));
            }

            cryptoWalletsAdapter = new CryptoWalletsAdapter(walletItems, getActivity());
            ApplicationLoader.applicationHandler.post(() -> walletsRecView.setAdapter(cryptoWalletsAdapter));

            final HashMap<String, HashMap<String, ExchangeRatesItem>> map = ExchangeRates.getInstance().parseExchangeRates();
            if (map.size() > 0) {
                exchangeRates.clear();
                exchangeRates.putAll(map);
                exchangeRatesAdapter = new ExchangeRatesAdapter(exchangeRatesItems);
                ApplicationLoader.applicationHandler.post(() -> exchangeRatesRecView.setAdapter(exchangeRatesAdapter));
            }
            ApplicationLoader.applicationHandler.post(this::stopLoaderAnimation);
        });
    }

    private void changeFiatCurrency() {
        Utils.stageQueue.postRunnable(() -> {
            final String currentFiatCurrency = spinner.getSelectedItem().toString();
            ArrayList<ExchangeRatesItem> exchangeRatesItems = new ArrayList<>();
            for (final String cryptoCurrency : exchangeRates.keySet()) {
                final HashMap<String, ExchangeRatesItem> exchangeRate = exchangeRates.get(cryptoCurrency);
                final String value = exchangeRate.get(currentFiatCurrency).value;
                exchangeRatesItems.add(new ExchangeRatesItem(cryptoCurrency, currentFiatCurrency, value));
            }
            exchangeRatesAdapter = new ExchangeRatesAdapter(exchangeRatesItems);
            ApplicationLoader.applicationHandler.post(() -> exchangeRatesRecView.setAdapter(exchangeRatesAdapter));

            final String ethExRate = exchangeRates.get("ETH").get(currentFiatCurrency).value;
            for (WalletItem wallet : cryptoWalletsAdapter.walletItems) {
                wallet.fiatCurrency = currentFiatCurrency;
                if (wallet.cryptoCurrency.equals("ETH")) {
                    wallet.fiatBalance = Ethereum.getInstance().calculateFiatBalance(ethExRate);
                } else if (wallet.cryptoCurrency.equals("BTC")) {
                    //       TODO:
                } else if (wallet.cryptoCurrency.equals("PMNT")) {
                    //TODO:
                }
            }
            ApplicationLoader.applicationHandler.post(() -> cryptoWalletsAdapter.notifyDataSetChanged());
        });
    }

    private class LoaderHandler extends Thread implements Runnable {
        boolean running = true;

        @Override
        public void run() {
            int loaderIndex = 0;
            String[] loaderValues = new String[]{getActivity().getString(R.string.loading_one), getActivity().getString(R.string.loading_two), getActivity().getString(R.string.loading_three)};
            ApplicationLoader.applicationHandler.post(() -> loader.setVisibility(View.VISIBLE));
            while (running) {
                switch (loaderIndex) {
                    case 0:
                        loaderIndex = 1;
                        ApplicationLoader.applicationHandler.post(() -> loader.setText(loaderValues[0]));
                        break;
                    case 1:
                        loaderIndex = 2;
                        ApplicationLoader.applicationHandler.post(() -> loader.setText(loaderValues[1]));
                        break;
                    case 2:
                        ApplicationLoader.applicationHandler.post(() -> loader.setText(loaderValues[2]));
                        loaderIndex = 0;
                        break;
                }

                try {
                    Thread.sleep(450);
                } catch (InterruptedException e) {

                }
            }
        }

        public void halt() {
            running = false;
        }
    }
}
