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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.adapters.CryptoWalletsAdapter;
import ru.paymon.android.adapters.ExchangeRatesAdapter;
import ru.paymon.android.components.CustomDialogProgress;
import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.MoneyViewModel;

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class FragmentMoney extends Fragment implements NotificationManager.IListener {
    public static final String CURRENCY_KEY = "CURRENCY_KEY";

    private CustomDialogProgress dialogProgress;
    private RecyclerView exchangeRatesRecView;
    private MoneyViewModel moneyViewModel;
    private ExchangeRatesAdapter exchangeRatesAdapter;
    private RecyclerView walletsRecView;
    private CryptoWalletsAdapter cryptoWalletsAdapter;
    private LiveData<List<ExchangeRate>> exchangeRatesData;
    private LiveData<ArrayList<WalletItem>> walletsData;
    private LiveData<Boolean> showProgress;
    private String currentCurrency = "USD";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moneyViewModel = ViewModelProviders.of(getActivity()).get(MoneyViewModel.class);
        showProgress = moneyViewModel.getProgressState();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_money, container, false);

        exchangeRatesRecView = (RecyclerView) view.findViewById(R.id.fragment_money_exchange_rates);
        walletsRecView = (RecyclerView) view.findViewById(R.id.fragment_money_wallets);
        TextView updateButton = (TextView) view.findViewById(R.id.fragment_money_update);
        Button usdButton = (Button) view.findViewById(R.id.fragment_money_currency_usd);
        Button eurButton = (Button) view.findViewById(R.id.fragment_money_currency_eur);
        Button localButton = (Button) view.findViewById(R.id.fragment_money_currency_local);
        ImageView usdBacklight = (ImageView) view.findViewById(R.id.fragment_money_currency_usd_backlight);
        ImageView eurBacklight = (ImageView) view.findViewById(R.id.fragment_money_currency_eur_backlight);
        ImageView localBacklight = (ImageView) view.findViewById(R.id.fragment_money_currency_local_backlight);

        dialogProgress = new CustomDialogProgress(getContext(), ApplicationLoader.applicationContext.getString(R.string.wallet_loader), R.drawable.cryptocurrency);
        dialogProgress.setCancelable(false);

        exchangeRatesRecView.setHasFixedSize(true);
        exchangeRatesRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        walletsRecView.setHasFixedSize(true);
        walletsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        final String localCurrency = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        localButton.setText(localCurrency);
        localButton.setVisibility(localCurrency.equals("USD") || localCurrency.equals("EUR") ? View.GONE : View.VISIBLE);

        usdButton.setOnClickListener((v) -> {
            currentCurrency = getString(R.string.usd);
            usdBacklight.setBackgroundColor(getResources().getColor(R.color.blue_bright));
            eurBacklight.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            localBacklight.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            changeCurrency();
        });

        eurButton.setOnClickListener((v) -> {
            currentCurrency = getString(R.string.eur);
            usdBacklight.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            eurBacklight.setBackgroundColor(getResources().getColor(R.color.blue_bright));
            localBacklight.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            changeCurrency();
        });

        localButton.setOnClickListener((v) -> {
            currentCurrency = localCurrency;
            usdBacklight.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            eurBacklight.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            localBacklight.setBackgroundColor(getResources().getColor(R.color.blue_bright));
            changeCurrency();
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

        walletsData = moneyViewModel.getWalletsData();
        exchangeRatesData = moneyViewModel.getExchangeRatesData();

        walletsData.observe(getActivity(), (walletsData) -> {
            if(walletsData == null) return;
            cryptoWalletsAdapter = new CryptoWalletsAdapter(walletsData, cryptoWalletsListener);
            walletsRecView.setAdapter(cryptoWalletsAdapter);
            changeCurrency();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_FINISHED);
        Utils.showBottomBar(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_FINISHED);
    }

    private void showProgress() {
        dialogProgress.show();
    }

    private void hideProgress() {
        dialogProgress.dismiss();
    }

    private void changeCurrency() {
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

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_FINISHED) {
            walletsData = moneyViewModel.getWalletsData();
        }
    }
}
