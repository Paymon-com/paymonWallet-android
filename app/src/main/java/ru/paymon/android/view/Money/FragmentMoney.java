package ru.paymon.android.view.Money;

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
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.navigation.Navigation;
import ru.paymon.android.R;
import ru.paymon.android.adapters.CryptoWalletsAdapter;
import ru.paymon.android.adapters.ExchangeRatesAdapter;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.models.NonEmptyWalletItem;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.DialogProgress;
import ru.paymon.android.view.Money.ethereum.DialogFragmentCreateRestoreEthereumWallet;
import ru.paymon.android.viewmodels.MoneyViewModel;

public class FragmentMoney extends Fragment {
    private static FragmentMoney instance;
    private DialogProgress dialogProgress;
    private Spinner fiatCurrencySpinner;
    private RecyclerView exchangeRatesRecView;
    private MoneyViewModel moneyViewModel;
    private ExchangeRatesAdapter exchangeRatesAdapter;
    private CryptoWalletsAdapter cryptoWalletsAdapter;
    private LiveData<ArrayList<ExchangeRatesItem>> exchangeRatesData;
    private LiveData<ArrayList<WalletItem>> walletsData;
    private LiveData<Boolean> showProgress;
    private ArrayList<ExchangeRatesItem> exchangeRatesItems;
    private LiveData<String> ethereumBalanceData;


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
        ethereumBalanceData = moneyViewModel.getEthereumBalanceData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_money, container, false);

        exchangeRatesRecView = (RecyclerView) view.findViewById(R.id.fragment_money_exchange_rates);
        RecyclerView walletsRecView = (RecyclerView) view.findViewById(R.id.fragment_money_wallets);
        fiatCurrencySpinner = (Spinner) view.findViewById(R.id.fragment_money_spinner);
        TextView updateButton = (TextView) view.findViewById(R.id.fragment_money_update);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(false);

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
            cryptoWalletsAdapter = new CryptoWalletsAdapter(walletsData, cryptoWalletsListener);
            walletsRecView.setAdapter(cryptoWalletsAdapter);
        });

        ethereumBalanceData.observe(getActivity(), (balanceData) -> {
            ArrayList<WalletItem> walletItems = walletsData.getValue();
            if (walletItems == null)
                return;
            for (WalletItem walletItem : walletItems) {
                if (walletItem instanceof NonEmptyWalletItem) {
                    NonEmptyWalletItem wi = (NonEmptyWalletItem) walletItem;
                    if (wi.cryptoCurrency.equals("ETH")) {
                        wi.cryptoBalance = balanceData;
                    }
                }
            }
        });

        showProgress.observe(getActivity(), (flag) -> {
            if (flag == null) return;
            if (flag)
                showProgress();
            else
                hideProgress();
        });

        updateButton.setOnClickListener(view1 -> {
            if (showProgress.getValue() != null && !showProgress.getValue()) {
                moneyViewModel.getWalletsData();
                moneyViewModel.getExchangeRatesData();
            }
        });

        fiatCurrencySpinner.setOnItemSelectedListener(fiatCurrencyListener);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.showBottomBar(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void showProgress() {
        dialogProgress.show();
    }

    private void hideProgress() {
        dialogProgress.dismiss();
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

    private CryptoWalletsAdapter.IOnItemClickListener cryptoWalletsListener = new CryptoWalletsAdapter.IOnItemClickListener() {
        @Override
        public void onClick(String cryptoCurrency) {
            switch (cryptoCurrency) {
                case "BTC":
//                            fragment = FragmentBitcoinWallet.newInstance();
                    break;
                case "ETH":
                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentEthereumWallet);
                    break;
                case "PMNT":
//                            fragment = FragmentPaymonWallet.newInstance();
                    break;
            }
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
    };

    AdapterView.OnItemSelectedListener fiatCurrencyListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            changeCurrency();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}
