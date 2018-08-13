package ru.paymon.android.view;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.daimajia.androidviewhover.tools.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.adapters.CryptoWalletsAdapter;
import ru.paymon.android.models.Wallet;
import ru.paymon.android.utils.Utils;

public class FragmentMoney extends Fragment {
    private static FragmentMoney instance;
    private RecyclerView walletsRecView;
    private RecyclerView exchangeRatesRecView;

    public static synchronized FragmentMoney newInstance() {
        instance = new FragmentMoney();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_money, container, false);

        walletsRecView = (RecyclerView) view.findViewById(R.id.fragment_money_wallets);
        walletsRecView = (RecyclerView) view.findViewById(R.id.fragment_money_exchange_rates);

        initWallets();
        initExchangeRates();

        return view;
    }

    private void initWallets() {
        ArrayList<Wallet> wallets = new ArrayList<>();

        //TODO:получение инфы о кошельке юзера с etherscan
        wallets.add(new Wallet("BTC", "RUB", "0.00", "0.00", false));
        wallets.add(new Wallet("ETH", "RUB", "0.00", "0.00", false));
        wallets.add(new Wallet("PMNT", "RUB", "0.00", "0.00", false));
        wallets.add(new Wallet("BTC", "RUB", "0.00", "0.00", true));
        wallets.add(new Wallet("ETH", "RUB", "0.00", "0.00", true));
        wallets.add(new Wallet("PMNT", "RUB", "0.00", "0.00", true));

        walletsRecView.setHasFixedSize(true);
        walletsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        CryptoWalletsAdapter cryptoWalletsAdapter = new CryptoWalletsAdapter(wallets);
        walletsRecView.setAdapter(cryptoWalletsAdapter);
    }

    @SuppressLint("StaticFieldLeak")
    private void initExchangeRates() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String link = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=BTC,ETH,PMNT&tsyms=USD,EUR,RUB";

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
//                    for (String str : doubles) {
//                        JSONObject jsonObject = new JSONObject(stringBuilder.toString()).getJSONObject(str);
//                        courses.put(str + "_USD", jsonObject.get("USD"));
//                        courses.put(str + "_EUR", jsonObject.get("EUR"));
//                        courses.put(str + "_RUB", jsonObject.get("RUB"));
//                    }
//                } catch (JSONException | IOException ignored) {
//                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
        Utils.hideActionBar(getActivity());
//        //Utils.setActionBarWithTitle(getActivity(), "Деньги");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
