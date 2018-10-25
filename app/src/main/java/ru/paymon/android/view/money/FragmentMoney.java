package ru.paymon.android.view.money;

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
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import androidx.navigation.Navigation;
import ru.paymon.android.Config;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.adapters.CryptoWalletsAdapter;
import ru.paymon.android.adapters.ExchangeRatesAdapter;
import ru.paymon.android.gateway.exchangerates.ExchangeRate;
import ru.paymon.android.models.NonEmptyWalletItem;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.DialogProgress;
import ru.paymon.android.viewmodels.MoneyViewModel;

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class FragmentMoney extends Fragment {
    public static final String CURRENCY_KEY = "CURRENCY_KEY";

    private DialogProgress dialogProgress;
    private NumberPicker fiatCurrencySpinner;
    private RecyclerView exchangeRatesRecView;
    private MoneyViewModel moneyViewModel;
    private ExchangeRatesAdapter exchangeRatesAdapter;
    private CryptoWalletsAdapter cryptoWalletsAdapter;
    private LiveData<List<ExchangeRate>> exchangeRatesData;
    private LiveData<ArrayList<WalletItem>> walletsData;
    private LiveData<Boolean> showProgress;
    private LiveData<BigInteger> ethereumBalanceData;


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
        fiatCurrencySpinner = (NumberPicker) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_fiat_currency);

        TextView updateButton = (TextView) view.findViewById(R.id.fragment_money_update);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(false);

        exchangeRatesRecView.setHasFixedSize(true);
        exchangeRatesRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        walletsRecView.setHasFixedSize(true);
        walletsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        fiatCurrencySpinner.setMinValue(1);
        fiatCurrencySpinner.setMaxValue(Config.fiatCurrencies.length);
        fiatCurrencySpinner.setDisplayedValues(Config.fiatCurrencies);
        fiatCurrencySpinner.setValue(2);

        fiatCurrencySpinner.setOnValueChangedListener((NumberPicker picker, int oldVal, int newVal) -> changeCurrency());

        exchangeRatesData.observe(this, (exchangeRatesItems) -> {
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
                    if (wi.cryptoCurrency.equals(ETH_CURRENCY_VALUE)) {
                        wi.cryptoBalance = Convert.fromWei(new BigDecimal(balanceData), Convert.Unit.ETHER).toString();
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
        String currentCurrency = fiatCurrencySpinner.getDisplayedValues()[fiatCurrencySpinner.getValue() - 1];
        moneyViewModel.fiatCurrency = currentCurrency;
        List<ExchangeRate> exchangeRates = exchangeRatesData.getValue();
        if (exchangeRates == null || exchangeRates.size() <= 0)
            return;
        List<ExchangeRate> exRatesItems = new ArrayList<>();
        for (ExchangeRate exchangeRateItem : exchangeRates) {
            if (exchangeRateItem.fiatCurrency.equals(currentCurrency)) {
                exRatesItems.add(exchangeRateItem);
            }
        }
        exchangeRatesAdapter = new ExchangeRatesAdapter(exRatesItems);
        exchangeRatesRecView.setAdapter(exchangeRatesAdapter);
        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_FIAT_CURRENCY_CHANGED, currentCurrency);
        if (cryptoWalletsAdapter != null)
            cryptoWalletsAdapter.notifyDataSetChanged();
    }

    private CryptoWalletsAdapter.IOnItemClickListener cryptoWalletsListener = new CryptoWalletsAdapter.IOnItemClickListener() {
        @Override
        public void onClick(String cryptoCurrency) {
            switch (cryptoCurrency) {
                case ETH_CURRENCY_VALUE:
                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentEthereumWallet);
                    break;
                case BTC_CURRENCY_VALUE:
                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentBitcoinWallet);
                    break;
                case PMNT_CURRENCY_VALUE:
                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentPaymonWallet);
                    break;
            }
        }

        @Override
        public void onCreateClick(String cryptoCurrency) {
            Bundle bundle = new Bundle();
            bundle.putString(CURRENCY_KEY, cryptoCurrency);
            new DialogFragmentCreateRestoreWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null);
        }
    };

}
