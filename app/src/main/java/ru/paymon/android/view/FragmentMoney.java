package ru.paymon.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.adapters.CryptoWalletsAdapter;
import ru.paymon.android.adapters.ExchangeRatesAdapter;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.MoneyViewModel;

public class FragmentMoney extends Fragment {
    private static FragmentMoney instance;
    private ProgressBar progressBar;
    private Spinner fiatCurrencySpinner;
    private RecyclerView exchangeRatesRecView;
    private MoneyViewModel moneyViewModel;
    private ExchangeRatesAdapter exchangeRatesAdapter;
    private CryptoWalletsAdapter cryptoWalletsAdapter;
    private LiveData<ArrayList<ExchangeRatesItem>> exchangeRatesData;
    private LiveData<ArrayList<WalletItem>> walletsData;
    private LiveData<Boolean> showProgress;
    private ArrayList<ExchangeRatesItem> exchangeRatesItems;
    boolean isProgressShowed;

    public static FragmentMoney getInstance() {
        if (instance == null)
            instance = new FragmentMoney();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moneyViewModel = ViewModelProviders.of(getActivity()).get(MoneyViewModel.class);
        showProgress = moneyViewModel.getProgressState();
        walletsData = moneyViewModel.getWalletsData();
        exchangeRatesData = moneyViewModel.getExchangeRatesData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_money, container, false);

        exchangeRatesRecView = (RecyclerView) view.findViewById(R.id.fragment_money_exchange_rates);
        RecyclerView walletsRecView = (RecyclerView) view.findViewById(R.id.fragment_money_wallets);
        fiatCurrencySpinner = (Spinner) view.findViewById(R.id.fragment_money_spinner);
        TextView updateButton = (TextView) view.findViewById(R.id.fragment_money_update);
        progressBar = (ProgressBar) view.findViewById(R.id.fragment_money_progressbar);

        exchangeRatesRecView.setHasFixedSize(true);
        exchangeRatesRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        walletsRecView.setHasFixedSize(true);
        walletsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"USD", "EUR"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fiatCurrencySpinner.setAdapter(adapter);
        fiatCurrencySpinner.setSelection(0);

        exchangeRatesData.observe(this, (exchangeRatesItems) -> {
            this.exchangeRatesItems = exchangeRatesItems;
            changeCurrency();
        });

        walletsData.observe(this, (walletsData) -> {
            cryptoWalletsAdapter = new CryptoWalletsAdapter(walletsData, new CryptoWalletsAdapter.IOnItemClickListener() {
                @Override
                public void onClick(String cryptoCurrency) {
                    Fragment fragment = null;
                    switch (cryptoCurrency) {
                        case "BTC":
//                            fragment = FragmentBitcoinWallet.newInstance();
                            break;
                        case "ETH":
                            fragment = FragmentEthereumWallet.newInstance();
                            break;
                        case "PMNT":
//                            fragment = FragmentPaymonWallet.newInstance();
                            break;
                    }

                    if (fragment != null)
                        Utils.replaceFragmentWithAnimationFade(getActivity().getSupportFragmentManager(), fragment, null);
                }

                @Override
                public void onCreateClick(String cryptoCurrency) {
                    switch (cryptoCurrency) {
                        case "BTC":
//                            DialogFragmentCreateRestoreBitcoinWallet.newInstance().show(getActivity().getSupportFragmentManager(), null);
                            break;
                        case "ETH":
                            DialogFragmentCreateRestoreEthereumWallet.newInstance().show(getActivity().getSupportFragmentManager(), null);
                            break;
                        case "PMNT":
//                            DialogFragmentCreateRestorePaymonWallet.newInstance().show(getActivity().getSupportFragmentManager(), null);
                            break;
                    }
                }
            });
            walletsRecView.setAdapter(cryptoWalletsAdapter);
        });

        showProgress.observe(getActivity(), (flag) -> {
            isProgressShowed = flag;
            if (flag)
                showProgress();
            else
                hideProgress();
        });

        updateButton.setOnClickListener(view1 -> {
            if (!isProgressShowed) {
                moneyViewModel.getWalletsData();
                moneyViewModel.getExchangeRatesData();
            }
        });

        fiatCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeCurrency();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void changeCurrency() {
        if (exchangeRatesItems == null || exchangeRatesItems.size() <= 0)
            return;
        String currentCurrency = fiatCurrencySpinner.getSelectedItem().toString();
        ArrayList<ExchangeRatesItem> exRatesItems = new ArrayList<>();
        for (ExchangeRatesItem exchangeRateItem : exchangeRatesItems) {
            if (exchangeRateItem.fiatCurrency.equals(currentCurrency)) {
                exRatesItems.add(exchangeRateItem);
            }
        }
        exchangeRatesAdapter = new ExchangeRatesAdapter(exRatesItems);
        exchangeRatesRecView.setAdapter(exchangeRatesAdapter);
    }
}
