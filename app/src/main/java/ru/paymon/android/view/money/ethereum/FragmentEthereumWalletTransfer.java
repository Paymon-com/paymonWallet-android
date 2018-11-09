package ru.paymon.android.view.money.ethereum;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.ExchangeRatesManager;
import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.activities.QrCodeScannerActivity;
import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.MoneyViewModel;

import static android.app.Activity.RESULT_OK;
import static ru.paymon.android.activities.QrCodeScannerActivity.QR_SCAN_RESULT_KEY;
import static ru.paymon.android.activities.QrCodeScannerActivity.REQUEST_CODE_QR_SCANNER_START;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;

public class FragmentEthereumWalletTransfer extends Fragment {
    private EditText amountEditText;
    private TextView balanceTextView;
    private TextView feeTextView;
    private TextView totalTextView;
    private IndicatorSeekBar gasPriceBar;
    private IndicatorSeekBar gasLimitBar;
    private EditText receiverAddressEditText;
//    private NumberPicker fiatCurrencyPicker;
    private TextView fiatEqualTextView;

    private WalletApplication application;
    private MoneyViewModel moneyViewModel;
    private LiveData<BigInteger> ethereumBalanceData;
    private LiveData<Integer> midGasPriceData;
    private LiveData<Integer> maxGasPriceData;
    private String currentExchangeRate;
    private double ethAmount;
    private double ethFee;
    private double totalValueEth;
    private int gasPrice;
    private int gasLimit = Config.GAS_LIMIT_DEFAULT;
    private BigDecimal bigIntegerWeiFee;
    private BigDecimal bigIntegerWeiAmount;
    private BigDecimal bigIntegerWeiTotal;
    private String currentCurrency = "USD";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (WalletApplication) getActivity().getApplication();
        moneyViewModel = ViewModelProviders.of(getActivity()).get(MoneyViewModel.class);
        ethereumBalanceData = moneyViewModel.getEthereumBalanceData();
        moneyViewModel.updateMidAndMaxGasPriceData();
        midGasPriceData = moneyViewModel.getMidGasPriceData();
        maxGasPriceData = moneyViewModel.getMaxGasPriceData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethereum_wallet_transfer, container, false);

        receiverAddressEditText = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_receiver_address);
        amountEditText = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_amount);
//        fiatCurrencyPicker = (NumberPicker) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_currency);
        balanceTextView = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_balance);
        gasPriceBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_price_slider);
        gasLimitBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_limit_slider);
        feeTextView = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_network_fee_value);
        totalTextView = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_total_value);
        fiatEqualTextView = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_eq);
        TextView fromAddressTextView = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_id_from);
        ImageView qrScannerButton = (ImageView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_qr);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.toolbar_eth_wallet_transf_back_image_button);
        ImageButton payButton = (ImageButton) view.findViewById(R.id.toolbar_eth_wallet_transf_next_image_view);
        Button usdButton = (Button) view.findViewById(R.id.fragment_ethereum_wallet_usd);
        Button eurButton = (Button) view.findViewById(R.id.fragment_ethereum_wallet_eur);
        Button localButton = (Button) view.findViewById(R.id.fragment_ethereum_wallet_local);
        ImageView usdBacklight = (ImageView) view.findViewById(R.id.fragment_ethereum_wallet_usd_backlight);
        ImageView eurBacklight = (ImageView) view.findViewById(R.id.fragment_ethereum_wallet_eur_backlight);
        ImageView localBacklight = (ImageView) view.findViewById(R.id.fragment_ethereum_wallet_local_backlight);

        WalletApplication application = (WalletApplication) getActivity().getApplication();

//        fiatCurrencyPicker.setMinValue(1);
//        fiatCurrencyPicker.setMaxValue(Config.fiatCurrencies.length);
//        fiatCurrencyPicker.setDisplayedValues(Config.fiatCurrencies);
//        fiatCurrencyPicker.setOnValueChangedListener((NumberPicker picker, int oldVal, int newVal) -> changeCurrency());
//        fiatCurrencyPicker.setValue(2);

        gasPriceBar.setIndicatorTextFormat(getString(R.string.current_gas_price) + ": ${PROGRESS} GWEI");
        gasLimitBar.setIndicatorTextFormat(getString(R.string.current_gas_limit) + ": ${PROGRESS}");
        fromAddressTextView.setText(application.getEthereumWallet().publicAddress);

        final String localCurrency = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        localButton.setText(localCurrency);
        localButton.setVisibility(localCurrency.equals("USD") || localCurrency.equals("EUR") ? View.GONE : View.VISIBLE);

        changeCurrency();


        backButton.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        payButton.setOnClickListener(v -> pay());

        usdButton.setOnClickListener(v -> {
            currentCurrency = "USD";
            usdBacklight.setBackgroundColor(getResources().getColor(R.color.blue_bright));
            eurBacklight.setBackgroundColor(getResources().getColor(R.color.bg_dialog_title));
            localBacklight.setBackgroundColor(getResources().getColor(R.color.bg_dialog_title));
            changeCurrency();
        });

        eurButton.setOnClickListener(v -> {
            currentCurrency = "EUR";
            usdBacklight.setBackgroundColor(getResources().getColor(R.color.bg_dialog_title));
            eurBacklight.setBackgroundColor(getResources().getColor(R.color.blue_bright));
            localBacklight.setBackgroundColor(getResources().getColor(R.color.bg_dialog_title));
            changeCurrency();
        });

        localButton.setOnClickListener(v -> {
            currentCurrency = localCurrency;
            usdBacklight.setBackgroundColor(getResources().getColor(R.color.bg_dialog_title));
            eurBacklight.setBackgroundColor(getResources().getColor(R.color.bg_dialog_title));
            localBacklight.setBackgroundColor(getResources().getColor(R.color.blue_bright));
            changeCurrency();
        });

        final String fromAddress = application.getEthereumWallet().publicAddress;
        fromAddressTextView.setText(fromAddress);

        receiverAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();

                if (!Utils.verifyETHpubKey(value)) {
                    receiverAddressEditText.setError(getText(R.string.not_a_eth_address));
                } else {
                    receiverAddressEditText.setError(null);
                }
            }
        });

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String value = editable.toString();

                if (value.startsWith(".")) {
                    amountEditText.setText(null);
                    return;
                }

                if (value.isEmpty()) {
                    amountEditText.setError(getText(R.string.required_field));
                    return;
                }

                ethAmount = Double.parseDouble(value);

                amountEditText.setError(null);

                calculateFees();
                changeCurrency();
            }
        });

        gasPriceBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                gasPrice = seekParams.progress;
                calculateFees();
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        gasLimitBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                gasLimit = seekParams.progress;
                calculateFees();
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        ethereumBalanceData.observe(getActivity(), (balanceData) -> {
            if (balanceData != null)
                balanceTextView.setText(String.format("%s ETH", Convert.fromWei(new BigDecimal(balanceData), Convert.Unit.ETHER).toString()));
        });

        maxGasPriceData.observe(getActivity(), maxGasPrice -> {
            gasPriceBar.setMin(1);
            gasPriceBar.setMax(maxGasPrice != null ? maxGasPrice : 100);
            gasLimitBar.setMax(Config.GAS_LIMIT_MAX);
            gasLimitBar.setMin(Config.GAS_LIMIT_MIN);
            gasLimitBar.setProgress(Config.GAS_LIMIT_DEFAULT);
        });

        midGasPriceData.observe(getActivity(), midGasPrice -> {
            if (midGasPrice != null) {
                gasPriceBar.setProgress(midGasPrice);
                gasPrice = midGasPrice;
            } else {
                gasPriceBar.setProgress(10);
                gasPrice = 10;
            }
        });

        qrScannerButton.setOnClickListener(v -> {
            Intent qrScannerIntent = new Intent(getContext(), QrCodeScannerActivity.class);
            startActivityForResult(qrScannerIntent, REQUEST_CODE_QR_SCANNER_START);
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

    private void pay() {
        final String toAddress = receiverAddressEditText.getText().toString();

        if (toAddress.isEmpty() || !Utils.verifyETHpubKey(toAddress)) {
            return;
        }

        final BigInteger bigIntegerBalance = moneyViewModel.getEthereumBalanceData().getValue();
        if (bigIntegerBalance != null) {
            if (bigIntegerWeiTotal.toBigInteger().compareTo(bigIntegerBalance) == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
                        .setMessage(getText(R.string.insufficient_funds))
                        .setCancelable(true);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                final BigInteger bigIntegerGasPrice = new BigDecimal(gasPrice).toBigInteger();
                final BigInteger bigIntegerGasLimit = new BigDecimal(gasLimit).toBigInteger();
                Executors.newSingleThreadExecutor().submit(() -> {
                    EthSendTransaction ethSendTransaction = application.sendRawEthereumTx(toAddress, bigIntegerWeiAmount.toBigInteger(), bigIntegerGasPrice, bigIntegerGasLimit);
                    final String text = ethSendTransaction != null ? getString(R.string.transaction_hash) + ": " + ethSendTransaction.getTransactionHash() : getString(R.string.transaction_failed_to_send);
                    ApplicationLoader.applicationHandler.post(() -> {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
                                .setMessage(text)
                                .setCancelable(true);
                        AlertDialog alertDialog = builder2.create();
                        alertDialog.show();
                    });
                });
            }
        }
    }

    private void calculateFees() {
        BigDecimal bigDecimalWeiGasPrice = new BigDecimal(gasPrice).multiply(new BigDecimal(Math.pow(10, 9)));
        ethFee = Double.parseDouble(Convert.fromWei(new BigDecimal(gasLimit).multiply(bigDecimalWeiGasPrice), Convert.Unit.ETHER).toString());

        if (gasPrice != 0 && gasLimit != 0) {
            bigIntegerWeiFee = new BigDecimal(ethFee);
            feeTextView.setText(String.format("%.9f ETH", ethFee));
        } else {
            feeTextView.setText("0 ETH");
        }

        if (ethAmount != 0)
            bigIntegerWeiAmount = new BigDecimal(ethAmount).multiply(new BigDecimal(Math.pow(10, 18)));

        totalValueEth = ethFee + ethAmount;
        bigIntegerWeiTotal = (bigIntegerWeiAmount != null && bigIntegerWeiFee != null) ? bigIntegerWeiAmount.add(bigIntegerWeiFee) : bigIntegerWeiAmount != null ? bigIntegerWeiAmount : bigIntegerWeiFee;
        totalTextView.setText(String.format("%.9f ETH", totalValueEth));
    }

    private void changeCurrency() {
        if (bigIntegerWeiAmount == null) return;
//        final String currentFiatCurrency = fiatCurrencyPicker.getDisplayedValues()[fiatCurrencyPicker.getValue() - 1];
        final List<ExchangeRate> exchangeRates = ExchangeRatesManager.getInstance().getExchangeRatesByCryptoCurrency(ETH_CURRENCY_VALUE);
        for (ExchangeRate exchangeRate : exchangeRates) {
            if (exchangeRate.fiatCurrency.equals(currentCurrency))
                currentExchangeRate = exchangeRate.value;
        }
        final String fiatEqual = WalletApplication.convertEthereumToFiat(bigIntegerWeiAmount.toBigInteger(), currentExchangeRate);
        fiatEqualTextView.setText(String.format("%s %s", fiatEqual, currentCurrency));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getExtras() != null) {
                    if (data.getExtras().containsKey(QR_SCAN_RESULT_KEY)) {
                        String receiverAddress = data.getStringExtra(QR_SCAN_RESULT_KEY);
                        receiverAddressEditText.setText(receiverAddress);
                    }
                }
            }
        } else {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
                    .setMessage(getText(R.string.not_read_qr))
                    .setCancelable(true);
            android.support.v7.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
}
